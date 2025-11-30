from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from django.contrib.auth.models import User
from django.conf import settings
import requests
import json


@api_view(['POST'])
@permission_classes([AllowAny])
def auth0_login(request):
    """
    Endpoint para iniciar sesión con Auth0
    """
    try:
        # Obtener el código de autorización del request
        auth_code = request.data.get('code')
        
        if not auth_code:
            return Response(
                {'error': 'Código de autorización requerido'}, 
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Intercambiar código por token
        token_url = f"https://{settings.AUTH0_DOMAIN}/oauth/token"
        
        payload = {
            'grant_type': 'authorization_code',
            'client_id': settings.AUTH0_CLIENT_ID,
            'client_secret': settings.AUTH0_CLIENT_SECRET,
            'code': auth_code,
            'redirect_uri': 'http://localhost:3000/callback'  # URL de callback del frontend
        }
        
        response = requests.post(token_url, json=payload)
        
        if response.status_code == 200:
            token_data = response.json()
            return Response(token_data)
        else:
            return Response(
                {'error': 'Error al obtener token'}, 
                status=status.HTTP_400_BAD_REQUEST
            )
            
    except Exception as e:
        return Response(
            {'error': f'Error en el login: {str(e)}'}, 
            status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )


@api_view(['GET'])
@permission_classes([AllowAny])
def auth0_config(request):
    """
    Endpoint para obtener la configuración de Auth0 para el frontend
    """
    config = {
        'domain': settings.AUTH0_DOMAIN,
        'clientId': settings.AUTH0_CLIENT_ID,
        'audience': settings.API_IDENTIFIER,
        'redirectUri': 'http://localhost:3000/callback',
        'scope': 'openid profile email'
    }
    
    return Response(config)


@api_view(['GET'])
def user_profile(request):
    """
    Endpoint para obtener el perfil del usuario autenticado
    """
    # Verificar que el usuario esté autenticado
    if not request.user.is_authenticated:
        print("=== ERROR: Usuario no autenticado ===")
        return Response(
            {'error': 'Usuario no autenticado'}, 
            status=status.HTTP_401_UNAUTHORIZED
        )
    
    try:
        # Obtener el usuario directamente de la base de datos para asegurar datos actualizados
        user = User.objects.get(id=request.user.id)
        full_name = f"{user.first_name} {user.last_name}".strip() if (user.first_name or user.last_name) else None
        
        print(f"=== OBTENIENDO PERFIL ===")
        print(f"Usuario ID: {user.id}")
        print(f"Username: {user.username}")
        print(f"Nombre: {user.first_name} {user.last_name}")
        print(f"Email: {user.email}")
        print(f"Full name: {full_name}")
        
        return Response({
            'id': user.id,
            'username': user.username,
            'email': user.email,
            'first_name': user.first_name,
            'last_name': user.last_name,
            'full_name': full_name,
            'is_authenticated': user.is_authenticated
        })
    except User.DoesNotExist:
        print(f"=== ERROR: Usuario con ID {request.user.id} no existe ===")
        return Response(
            {'error': 'Usuario no encontrado'}, 
            status=status.HTTP_404_NOT_FOUND
        )
    except Exception as e:
        print(f"=== ERROR al obtener perfil: {str(e)} ===")
        return Response(
            {'error': f'Error al obtener perfil: {str(e)}'}, 
            status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )


@api_view(['PUT', 'PATCH'])
def update_profile(request):
    """
    Endpoint para actualizar el perfil del usuario autenticado
    """
    if not request.user.is_authenticated:
        return Response(
            {'success': False, 'error': 'Usuario no autenticado'}, 
            status=status.HTTP_401_UNAUTHORIZED
        )
    
    user = request.user
    first_name = request.data.get('first_name', '').strip()
    last_name = request.data.get('last_name', '').strip()
    
    # Validar que los campos no estén vacíos
    if not first_name or not last_name:
        return Response(
            {'success': False, 'error': 'El nombre y apellidos son requeridos'}, 
            status=status.HTTP_400_BAD_REQUEST
        )
    
    try:
        # Guardar los valores anteriores para logging
        old_first_name = user.first_name
        old_last_name = user.last_name
        
        user.first_name = first_name
        user.last_name = last_name
        user.save()
        
        # Verificar que se guardó correctamente
        user.refresh_from_db()
        print(f"=== ACTUALIZACIÓN DE PERFIL ===")
        print(f"Usuario: {user.username}")
        print(f"Nombre anterior: {old_first_name} {old_last_name}")
        print(f"Nombre nuevo: {user.first_name} {user.last_name}")
        print(f"Verificación después de save(): {user.first_name} {user.last_name}")
        
        return Response({
            'success': True,
            'message': 'Perfil actualizado correctamente',
            'user': {
                'id': user.id,
                'username': user.username,
                'email': user.email,
                'first_name': user.first_name,
                'last_name': user.last_name,
            }
        })
    except Exception as e:
        return Response(
            {'success': False, 'error': f'Error al actualizar el perfil: {str(e)}'}, 
            status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )


@api_view(['POST'])
def logout(request):
    """
    Endpoint para cerrar sesión
    """
    # En Auth0, el logout se maneja principalmente en el frontend
    # Aquí podemos limpiar cualquier sesión local si es necesario
    return Response({'message': 'Logout exitoso'})