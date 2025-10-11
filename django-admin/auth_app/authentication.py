import jwt
import requests
from django.conf import settings
from django.contrib.auth.models import User
from rest_framework import authentication, exceptions
from rest_framework.authentication import BaseAuthentication


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
            # Decodificar el token JWT
            payload = jwt.decode(
                token,
                self._get_jwks(),
                algorithms=settings.ALGORITHMS,
                audience=settings.API_IDENTIFIER,
                issuer=f"https://{settings.AUTH0_DOMAIN}/"
            )
            
            # Obtener o crear el usuario
            user = self._get_or_create_user(payload)
            return (user, token)
            
        except jwt.ExpiredSignatureError:
            raise exceptions.AuthenticationFailed('Token expirado')
        except jwt.InvalidTokenError:
            raise exceptions.AuthenticationFailed('Token inválido')
        except Exception as e:
            raise exceptions.AuthenticationFailed(f'Error de autenticación: {str(e)}')
    
    def _get_jwks(self):
        """
        Obtener las claves públicas de Auth0
        """
        jwks_url = f"https://{settings.AUTH0_DOMAIN}/.well-known/jwks.json"
        response = requests.get(jwks_url)
        jwks = response.json()
        
        # Convertir JWKS a formato que PyJWT puede usar
        public_keys = {}
        for key in jwks['keys']:
            kid = key['kid']
            public_keys[kid] = jwt.algorithms.RSAAlgorithm.from_jwk(key)
        
        return public_keys
    
    def _get_or_create_user(self, payload):
        """
        Obtener o crear un usuario basado en el payload del token
        """
        auth0_id = payload.get('sub')
        email = payload.get('email')
        name = payload.get('name', '')
        
        try:
            user = User.objects.get(username=auth0_id)
        except User.DoesNotExist:
            # Crear nuevo usuario
            user = User.objects.create_user(
                username=auth0_id,
                email=email,
                first_name=name.split(' ')[0] if name else '',
                last_name=' '.join(name.split(' ')[1:]) if len(name.split(' ')) > 1 else ''
            )
        
        return user
