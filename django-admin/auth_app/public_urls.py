from django.urls import path
from . import public_views

app_name = 'public'

urlpatterns = [
    # Endpoints públicos
    path('health/', public_views.health_check, name='health_check'),
    path('info/', public_views.api_info, name='api_info'),
]

