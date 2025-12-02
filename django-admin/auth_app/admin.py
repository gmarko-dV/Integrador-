from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from django.contrib.auth.models import User
from django.utils.html import format_html
from django.db.models import Count
from .models import Anuncio, Vehiculo, Imagen, Notificacion, HistorialBusqueda, CategoriaVehiculo


# ==================== ADMIN SITE PERSONALIZADO ====================

# Guardar la función original del index
_original_index = admin.site.index

def index_view_simple(request, extra_context=None):
    """Vista simple del dashboard con conteo de usuarios"""
    extra_context = extra_context or {}
    
    # Agregar estadísticas al contexto
    extra_context.update({
        'total_usuarios': User.objects.count(),
        'total_anuncios': Anuncio.objects.count(),
        'anuncios_activos': Anuncio.objects.filter(activo=True).count(),
    })
    
    # Llamar a la función original del admin
    return _original_index(request, extra_context=extra_context)

admin.site.index = index_view_simple


# ==================== CATEGORIA VEHICULO ADMIN ====================

@admin.register(CategoriaVehiculo)
class CategoriaVehiculoAdmin(admin.ModelAdmin):
    """Administración de categorías de vehículos"""
    list_display = ('nombre', 'codigo', 'activo', 'total_anuncios', 'fecha_creacion')
    list_filter = ('activo', 'fecha_creacion')
    search_fields = ('nombre', 'codigo', 'descripcion')
    readonly_fields = ('id_categoria', 'fecha_creacion')
    fieldsets = (
        ('Información Básica', {
            'fields': ('id_categoria', 'nombre', 'codigo', 'descripcion', 'activo')
        }),
        ('Fechas', {
            'fields': ('fecha_creacion',)
        }),
    )
    
    def total_anuncios(self, obj):
        count = obj.anuncios.count()
        return format_html('<strong>{}</strong>', count)
    total_anuncios.short_description = 'Total Anuncios'


# ==================== USER ADMIN SIMPLIFICADO ====================

class UserAdminSimple(BaseUserAdmin):
    """Admin simplificado de usuarios"""
    list_display = ('username', 'email', 'first_name', 'last_name', 'total_anuncios', 'is_active', 'date_joined')
    list_filter = ('is_active', 'is_staff', 'date_joined')
    search_fields = ('username', 'email', 'first_name', 'last_name')
    
    def total_anuncios(self, obj):
        count = Anuncio.objects.filter(id_usuario=str(obj.id)).count()
        return format_html('<strong>{}</strong>', count)
    total_anuncios.short_description = 'Anuncios'


admin.site.unregister(User)
admin.site.register(User, UserAdminSimple)


# ==================== ANUNCIO ADMIN SIMPLIFICADO ====================

class ImagenInline(admin.TabularInline):
    """Inline para gestionar imágenes dentro de un anuncio"""
    model = Imagen
    extra = 0
    fields = ('url_imagen', 'nombre_archivo', 'orden')
    readonly_fields = ('fecha_subida',)


@admin.register(Anuncio)
class AnuncioAdminSimple(admin.ModelAdmin):
    """Administración simplificada de anuncios/publicaciones"""
    list_display = (
        'id_anuncio',
        'titulo_display',
        'modelo',
        'anio',
        'precio_display',
        'categoria',
        'activo_display',
        'fecha_creacion',
    )
    list_filter = ('activo', 'categoria', 'fecha_creacion', 'anio')
    search_fields = ('titulo', 'modelo', 'descripcion', 'id_usuario')
    readonly_fields = ('id_anuncio', 'fecha_creacion', 'fecha_actualizacion')
    fieldsets = (
        ('Información Básica', {
            'fields': ('id_anuncio', 'id_usuario', 'titulo', 'modelo', 'anio', 'categoria', 'tipo_vehiculo')
        }),
        ('Detalles', {
            'fields': ('kilometraje', 'precio', 'descripcion')
        }),
        ('Contacto', {
            'fields': ('email_contacto', 'telefono_contacto')
        }),
        ('Estado', {
            'fields': ('activo', 'fecha_creacion', 'fecha_actualizacion')
        }),
    )
    inlines = [ImagenInline]
    date_hierarchy = 'fecha_creacion'
    list_per_page = 25
    actions = ['activar_anuncios', 'desactivar_anuncios']

    def titulo_display(self, obj):
        return obj.titulo or f"{obj.modelo} {obj.anio}"
    titulo_display.short_description = 'Título'

    def precio_display(self, obj):
        precio_formateado = f"{obj.precio:.2f}"
        return format_html('<strong style="color: green;">S/ {}</strong>', precio_formateado)
    precio_display.short_description = 'Precio'

    def activo_display(self, obj):
        if obj.activo:
            return format_html('<span style="color: green;">✓ Activo</span>')
        return format_html('<span style="color: red;">✗ Inactivo</span>')
    activo_display.short_description = 'Estado'

    @admin.action(description='Activar anuncios seleccionados')
    def activar_anuncios(self, request, queryset):
        updated = queryset.update(activo=True)
        self.message_user(request, f'{updated} anuncio(s) activado(s).')

    @admin.action(description='Desactivar anuncios seleccionados')
    def desactivar_anuncios(self, request, queryset):
        updated = queryset.update(activo=False)
        self.message_user(request, f'{updated} anuncio(s) desactivado(s).')


# ==================== OTROS MODELOS (SIMPLIFICADOS) ====================

@admin.register(Vehiculo)
class VehiculoAdmin(admin.ModelAdmin):
    list_display = ('placa', 'marca', 'modelo', 'tipo_vehiculo', 'fecha_actualizacion_api')
    list_filter = ('marca', 'tipo_vehiculo')
    search_fields = ('placa', 'marca', 'modelo')


@admin.register(Imagen)
class ImagenAdmin(admin.ModelAdmin):
    list_display = ('id_imagen', 'anuncio', 'nombre_archivo', 'orden', 'fecha_subida')
    list_filter = ('fecha_subida',)
    search_fields = ('nombre_archivo', 'anuncio__titulo')


@admin.register(Notificacion)
class NotificacionAdmin(admin.ModelAdmin):
    list_display = ('id_notificacion', 'titulo', 'tipo', 'leida_display', 'fecha_creacion')
    list_filter = ('tipo', 'leida', 'fecha_creacion')
    search_fields = ('titulo', 'mensaje')
    
    def leida_display(self, obj):
        if obj.leida:
            return format_html('<span style="color: green;">✓ Leída</span>')
        return format_html('<span style="color: orange;">○ No leída</span>')
    leida_display.short_description = 'Estado'


@admin.register(HistorialBusqueda)
class HistorialBusquedaAdmin(admin.ModelAdmin):
    list_display = ('id_historial', 'placa_consultada', 'fecha_consulta')
    list_filter = ('fecha_consulta',)
    search_fields = ('placa_consultada', 'id_usuario')


# ==================== CONFIGURACIÓN DEL SITIO ====================

admin.site.site_header = '🚗 Panel de Administración - checkAuto'
admin.site.site_title = 'checkAuto Admin'
admin.site.index_title = 'Dashboard'
