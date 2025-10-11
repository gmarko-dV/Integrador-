from django.urls import path
from . import views, public_views

app_name = 'auth_app'

urlpatterns = [
    # Endpoints de autenticación
    path('login/', views.auth0_login, name='auth0_login'),
    path('config/', views.auth0_config, name='auth0_config'),
    path('profile/', views.user_profile, name='user_profile'),
    path('logout/', views.logout, name='logout'),
    
    # Endpoints públicos
    path('health/', public_views.health_check, name='health_check'),
    path('info/', public_views.api_info, name='api_info'),
]
