from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response


@api_view(['GET'])
@permission_classes([AllowAny])
def health_check(request):
    """
    Endpoint de salud para verificar que el servicio está funcionando
    """
    return Response({
        'status': 'OK',
        'service': 'Django Auth0 Backend',
        'message': 'Servicio funcionando correctamente'
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def api_info(request):
    """
    Endpoint con información de la API
    """
    return Response({
        'message': 'API Django con Auth0 disponible',
        'endpoints': {
            'auth_config': '/api/auth/config/',
            'login': '/api/auth/login/',
            'profile': '/api/auth/profile/',
            'logout': '/api/auth/logout/',
            'health': '/api/public/health/'
        }
    })
