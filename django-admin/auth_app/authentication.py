import jwt
import json
import requests
from django.conf import settings
from django.contrib.auth.models import User
from rest_framework import authentication, exceptions
from rest_framework.authentication import BaseAuthentication
import logging

logger = logging.getLogger(__name__)


class Auth0Authentication(BaseAuthentication):
    """
    Autenticación personalizada para Auth0
    """
    
    def authenticate(self, request):
        auth_header = request.META.get('HTTP_AUTHORIZATION')
        
        if not auth_header:
            return None
            
        try:
            token = auth_header.split(' ')[1]  # Bearer <token>
        except IndexError:
            return None
            
        try:
            # Verificar que el token sea un JWT válido (formato: header.payload.signature)
            if not token or len(token.split('.')) != 3:
                raise exceptions.AuthenticationFailed('Token no es un JWT válido')
            
            # Obtener el header del token para extraer el kid
            unverified_header = jwt.get_unverified_header(token)
            kid = unverified_header.get('kid')
            
            if not kid:
                raise exceptions.AuthenticationFailed('Token no contiene kid en el header JWT')
            
            # Obtener la clave pública correcta usando el kid
            public_key = self._get_public_key(kid)
            
            if not public_key:
                raise exceptions.AuthenticationFailed('No se pudo obtener la clave pública para verificar el token')
            
            # Decodificar el token JWT (ID tokens no tienen audience)
            payload = jwt.decode(
                token,
                public_key,
                algorithms=settings.ALGORITHMS,
                issuer=f"https://{settings.AUTH0_DOMAIN}/",
                options={"verify_aud": False}
            )
            
            # Validar dominio institucional del email
            email = payload.get('email')
            allowed_domain = getattr(settings, 'INSTITUTIONAL_EMAIL_DOMAIN', None)
            if not email or not allowed_domain or not email.endswith(f"@{allowed_domain}"):
                raise exceptions.AuthenticationFailed('Email no pertenece al dominio institucional permitido')

            # Obtener o crear el usuario
            user = self._get_or_create_user(payload)
            return (user, token)
            
        except jwt.ExpiredSignatureError:
            raise exceptions.AuthenticationFailed('Token expirado')
        except jwt.InvalidTokenError as e:
            logger.error(f'Error de token inválido: {str(e)}')
            raise exceptions.AuthenticationFailed('Token inválido')
        except Exception as e:
            logger.error(f'Error de autenticación: {str(e)}', exc_info=True)
            raise exceptions.AuthenticationFailed(f'Error de autenticación: {str(e)}')
    
    def _get_jwks(self):
        """
        Obtener las claves públicas de Auth0 y cachearlas
        """
        jwks_url = f"https://{settings.AUTH0_DOMAIN}/.well-known/jwks.json"
        try:
            response = requests.get(jwks_url, timeout=5)
            response.raise_for_status()
            jwks = response.json()
            
            # Convertir JWKS a formato que PyJWT puede usar
            public_keys = {}
            for key in jwks.get('keys', []):
                kid = key.get('kid')
                if kid:
                    public_keys[kid] = jwt.algorithms.RSAAlgorithm.from_jwk(json.dumps(key))
            
            return public_keys
        except Exception as e:
            logger.error(f'Error al obtener JWKS: {str(e)}')
            return {}
    
    def _get_public_key(self, kid):
        """
        Obtener la clave pública específica usando el kid
        """
        # Obtener todas las claves
        public_keys = self._get_jwks()
        return public_keys.get(kid)
    
    def _get_or_create_user(self, payload):
        """
        Obtener o crear un usuario basado en el payload del token
        """
        auth0_id = payload.get('sub')
        email = payload.get('email')
        name = payload.get('name', '')
        
        if not auth0_id:
            raise exceptions.AuthenticationFailed('Token no contiene sub (user ID)')
        
        try:
            user = User.objects.get(username=auth0_id)
            # Actualizar email si cambió
            if email and user.email != email:
                user.email = email
                user.save()
        except User.DoesNotExist:
            # Crear nuevo usuario
            name_parts = name.split(' ', 1) if name else ['', '']
            first_name = name_parts[0] if len(name_parts) > 0 else ''
            last_name = name_parts[1] if len(name_parts) > 1 else ''
            
            user = User(
                username=auth0_id,
                email=email or '',
                first_name=first_name,
                last_name=last_name
            )
            user.set_unusable_password()
            user.save()
            logger.info(f'Usuario creado: {auth0_id} (ID: {user.id})')
        
        return user
