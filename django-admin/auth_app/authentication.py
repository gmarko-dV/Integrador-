import jwt
from django.conf import settings
from django.contrib.auth.models import User
from rest_framework import exceptions
from rest_framework.authentication import BaseAuthentication
import logging

logger = logging.getLogger(__name__)


class SupabaseAuthentication(BaseAuthentication):
    """
    Autenticación personalizada para Supabase usando JWT
    """
    
    def authenticate(self, request):
        auth_header = request.META.get('HTTP_AUTHORIZATION')
        
        if not auth_header:
            return None
            
        try:
            parts = auth_header.split(' ')
            if len(parts) != 2 or parts[0].lower() != 'bearer':
                return None
            token = parts[1]
        except IndexError:
            return None
            
        try:
            # Verificar que el token sea un JWT válido (formato: header.payload.signature)
            if not token or len(token.split('.')) != 3:
                raise exceptions.AuthenticationFailed('Token no es un JWT válido')
            
            # Decodificar el token sin verificar primero para obtener el payload
            # Supabase firma tokens con HS256 usando el JWT secret
            supabase_url = getattr(settings, 'SUPABASE_URL', '')
            
            # Intentar decodificar con diferentes métodos
            payload = None
            
            # Método 1: Decodificar sin verificación (para desarrollo)
            # En producción, deberías usar el JWT secret real de Supabase
            try:
                payload = jwt.decode(
                    token,
                    options={
                        "verify_signature": False,
                        "verify_aud": False,
                        "verify_iss": False
                    }
                )
            except jwt.DecodeError as e:
                logger.error(f'Error decodificando token: {str(e)}')
                raise exceptions.AuthenticationFailed('Token inválido')
            
            if not payload:
                raise exceptions.AuthenticationFailed('No se pudo decodificar el token')
            
            # Verificar que el token no haya expirado
            import time
            exp = payload.get('exp')
            if exp and exp < time.time():
                raise exceptions.AuthenticationFailed('Token expirado')
            
            # Validar dominio institucional del email (opcional)
            email = payload.get('email')
            allowed_domain = getattr(settings, 'INSTITUTIONAL_EMAIL_DOMAIN', None)
            if allowed_domain and email and not email.endswith(f"@{allowed_domain}"):
                logger.warning(f'Email {email} no pertenece al dominio permitido {allowed_domain}')
                # No fallar, solo loguear advertencia

            # Obtener o crear el usuario
            user = self._get_or_create_user(payload)
            return (user, token)
            
        except jwt.ExpiredSignatureError:
            raise exceptions.AuthenticationFailed('Token expirado')
        except jwt.InvalidTokenError as e:
            logger.error(f'Error de token inválido: {str(e)}')
            raise exceptions.AuthenticationFailed('Token inválido')
        except exceptions.AuthenticationFailed:
            raise
        except Exception as e:
            logger.error(f'Error de autenticación: {str(e)}', exc_info=True)
            raise exceptions.AuthenticationFailed(f'Error de autenticación: {str(e)}')
    
    def _get_or_create_user(self, payload):
        """
        Obtener o crear un usuario basado en el payload del token de Supabase
        """
        supabase_id = payload.get('sub')
        email = payload.get('email')
        
        # Supabase almacena metadata en user_metadata
        user_metadata = payload.get('user_metadata', {})
        name = (
            payload.get('name') or 
            user_metadata.get('nombre') or 
            user_metadata.get('full_name') or 
            user_metadata.get('name') or 
            ''
        )
        
        if not supabase_id:
            raise exceptions.AuthenticationFailed('Token no contiene sub (user ID)')
        
        try:
            user = User.objects.get(username=supabase_id)
            
            # NO actualizar el nombre si el usuario ya tiene nombre en Django
            has_name_in_django = bool(user.first_name or user.last_name)
            
            if has_name_in_django:
                logger.debug(f'Usuario {supabase_id} ya tiene nombre en Django ({user.first_name} {user.last_name}), preservando...')
            else:
                # Solo si el usuario no tiene nombre, actualizarlo desde Supabase
                if name:
                    name_parts = name.split(' ', 1) if name else ['', '']
                    first_name = name_parts[0] if len(name_parts) > 0 else ''
                    last_name = name_parts[1] if len(name_parts) > 1 else ''
                    if first_name or last_name:
                        user.first_name = first_name
                        user.last_name = last_name
                        user.save()
                        logger.info(f'Nombre inicial establecido desde Supabase para usuario {supabase_id}')
            
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
                username=supabase_id,
                email=email or '',
                first_name=first_name,
                last_name=last_name
            )
            user.set_unusable_password()
            user.save()
            logger.info(f'Usuario creado desde Supabase: {supabase_id} (ID: {user.id})')
        
        return user
