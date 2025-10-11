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
    user = request.user
    return Response({
        'id': user.id,
        'username': user.username,
        'email': user.email,
        'first_name': user.first_name,
        'last_name': user.last_name,
        'is_authenticated': user.is_authenticated
    })


@api_view(['POST'])
def logout(request):
    """
    Endpoint para cerrar sesión
    """
    # En Auth0, el logout se maneja principalmente en el frontend
    # Aquí podemos limpiar cualquier sesión local si es necesario
    return Response({'message': 'Logout exitoso'})