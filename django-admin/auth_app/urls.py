from django.urls import path
from . import views

app_name = 'auth_app'

urlpatterns = [
    # Endpoints de autenticación (Supabase)
    path('profile/', views.user_profile, name='user_profile'),
    path('profile/update/', views.update_profile, name='update_profile'),
    path('logout/', views.logout, name='logout'),
]
