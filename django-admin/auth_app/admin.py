from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from django.contrib.auth.models import User
from django.utils.html import format_html
from django.urls import path
from django.shortcuts import render
from django.db.models import Count, Sum, Avg, Max, Min, Q
from django.utils import timezone
from datetime import timedelta
from .models import Anuncio, Vehiculo, Imagen, Notificacion, HistorialBusqueda


# ==================== ADMIN SITE PERSONALIZADO ====================

# Extender el AdminSite por defecto para mostrar reportes como página principal
def reportes_view_for_index(request):
    """Vista de reportes que se usará como página principal"""
    # Estadísticas generales
    total_anuncios = Anuncio.objects.count()
    anuncios_activos = Anuncio.objects.filter(activo=True).count()
    anuncios_inactivos = Anuncio.objects.filter(activo=False).count()
    
    # Estadísticas de precios
    precio_promedio = Anuncio.objects.aggregate(avg=Avg('precio'))['avg'] or 0
    precio_maximo_obj = Anuncio.objects.aggregate(max=Max('precio'))
    precio_maximo = precio_maximo_obj['max'] if precio_maximo_obj['max'] else 0
    precio_minimo = Anuncio.objects.order_by('precio').first()
    precio_minimo_valor = precio_minimo.precio if precio_minimo else 0
    
    # Estadísticas por tipo de vehículo
    por_tipo = Anuncio.objects.values('tipo_vehiculo').annotate(
        total=Count('id_anuncio'),
        precio_promedio=Avg('precio')
    ).order_by('-total')
    
    # Anuncios por mes
    ahora = timezone.now()
    ultimos_6_meses = []
    for i in range(6):
        mes = ahora - timedelta(days=30*i)
        count = Anuncio.objects.filter(
            fecha_creacion__year=mes.year,
            fecha_creacion__month=mes.month
        ).count()
        ultimos_6_meses.append({
            'mes': mes.strftime('%Y-%m'),
            'total': count
        })
    
    # Anuncios recientes
    anuncios_recientes = Anuncio.objects.order_by('-fecha_creacion')[:10]
    
    # Top usuarios por anuncios
    top_usuarios = Anuncio.objects.values('id_usuario').annotate(
        total=Count('id_anuncio')
    ).order_by('-total')[:10]
    
    context = {
        'total_anuncios': total_anuncios,
        'anuncios_activos': anuncios_activos,
        'anuncios_inactivos': anuncios_inactivos,
        'precio_promedio': precio_promedio,
        'precio_maximo': precio_maximo,
        'precio_minimo': precio_minimo_valor,
        'por_tipo': por_tipo,
        'ultimos_6_meses': ultimos_6_meses,
        'anuncios_recientes': anuncios_recientes,
        'top_usuarios': top_usuarios,
    }
    
    return render(request, 'admin/reportes_anuncios.html', context)

# Reemplazar el método index para mostrar reportes
admin.site.index = reportes_view_for_index


# ==================== INLINE ADMIN ====================

class ImagenInline(admin.TabularInline):
    """Inline para gestionar imágenes dentro de un anuncio"""
    model = Imagen
    extra = 0
    fields = ('url_imagen', 'nombre_archivo', 'tipo_archivo', 'tamano_archivo', 'orden', 'fecha_subida')
    readonly_fields = ('fecha_subida',)
    ordering = ('orden',)


# ==================== ANUNCIO ADMIN ====================

class AnuncioAdmin(admin.ModelAdmin):
    """Administración completa de anuncios"""
    list_display = (
        'id_anuncio', 
        'titulo_display', 
        'modelo', 
        'anio', 
        'precio_display', 
        'kilometraje', 
        'tipo_vehiculo',
        'total_imagenes_display',
        'activo_display',
        'fecha_creacion',
        'usuario_display'
    )
    list_filter = (
        'activo',
        'tipo_vehiculo',
        'fecha_creacion',
        'anio',
    )
    search_fields = (
        'titulo',
        'modelo',
        'id_usuario',
        'email_contacto',
        'telefono_contacto',
        'descripcion',
    )
    readonly_fields = (
        'id_anuncio',
        'fecha_creacion',
        'fecha_actualizacion',
        'total_imagenes_display',
    )
    fieldsets = (
        ('Información Básica', {
            'fields': ('id_anuncio', 'id_usuario', 'titulo', 'modelo', 'anio', 'tipo_vehiculo')
        }),
        ('Detalles del Vehículo', {
            'fields': ('kilometraje', 'precio', 'descripcion')
        }),
        ('Contacto', {
            'fields': ('email_contacto', 'telefono_contacto')
        }),
        ('Estado y Fechas', {
            'fields': ('activo', 'fecha_creacion', 'fecha_actualizacion', 'total_imagenes_display')
        }),
    )
    inlines = [ImagenInline]
    date_hierarchy = 'fecha_creacion'
    list_per_page = 25
    actions = ['activar_anuncios', 'desactivar_anuncios', 'exportar_anuncios']

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

    def total_imagenes_display(self, obj):
        count = obj.imagenes.count()
        return format_html('<strong>{}</strong> imagen(es)', count)
    total_imagenes_display.short_description = 'Total Imágenes'

    def usuario_display(self, obj):
        return format_html('<code>{}</code>', obj.id_usuario[:20] + '...' if len(obj.id_usuario) > 20 else obj.id_usuario)
    usuario_display.short_description = 'Usuario'

    @admin.action(description='Activar anuncios seleccionados')
    def activar_anuncios(self, request, queryset):
        updated = queryset.update(activo=True)
        self.message_user(request, f'{updated} anuncio(s) activado(s) exitosamente.')

    @admin.action(description='Desactivar anuncios seleccionados')
    def desactivar_anuncios(self, request, queryset):
        updated = queryset.update(activo=False)
        self.message_user(request, f'{updated} anuncio(s) desactivado(s) exitosamente.')

    @admin.action(description='Exportar información de anuncios seleccionados')
    def exportar_anuncios(self, request, queryset):
        # Esta acción puede ser extendida para exportar a CSV/Excel
        self.message_user(request, f'Preparando exportación de {queryset.count()} anuncio(s)...')

    def changelist_view(self, request, extra_context=None):
        extra_context = extra_context or {}
        extra_context['show_reportes_link'] = True
        return super().changelist_view(request, extra_context=extra_context)


# ==================== VEHICULO ADMIN ====================

@admin.register(Vehiculo)
class VehiculoAdmin(admin.ModelAdmin):
    """Administración de vehículos consultados por placa"""
    list_display = (
        'placa',
        'marca',
        'modelo',
        'anio_registro_api',
        'tipo_vehiculo',
        'fecha_actualizacion_api',
        'tiene_datos_api',
    )
    list_filter = (
        'marca',
        'tipo_vehiculo',
        'fecha_actualizacion_api',
        'fecha_registro_api',
    )
    search_fields = (
        'placa',
        'marca',
        'modelo',
        'vin',
        'propietario',
    )
    readonly_fields = (
        'id_vehiculo',
        'fecha_actualizacion_api',
        'datos_api_display',
    )
    fieldsets = (
        ('Información de Placa', {
            'fields': ('id_vehiculo', 'placa', 'tipo_vehiculo')
        }),
        ('Información del Vehículo', {
            'fields': ('marca', 'modelo', 'anio_registro_api', 'vin', 'uso')
        }),
        ('Información del Propietario', {
            'fields': ('propietario', 'delivery_point')
        }),
        ('Datos de API', {
            'fields': ('descripcion_api', 'image_url_api', 'fecha_registro_api', 'fecha_actualizacion_api', 'datos_api_display')
        }),
    )
    date_hierarchy = 'fecha_actualizacion_api'
    list_per_page = 25

    def tiene_datos_api(self, obj):
        return 'Sí' if obj.datos_api else 'No'
    tiene_datos_api.short_description = 'Tiene Datos API'
    tiene_datos_api.boolean = True

    def datos_api_display(self, obj):
        if obj.datos_api:
            import json
            return format_html('<pre>{}</pre>', json.dumps(obj.datos_api, indent=2, ensure_ascii=False))
        return 'Sin datos'
    datos_api_display.short_description = 'Datos API (JSON)'


# ==================== IMAGEN ADMIN ====================

@admin.register(Imagen)
class ImagenAdmin(admin.ModelAdmin):
    """Administración de imágenes de anuncios"""
    list_display = (
        'id_imagen',
        'anuncio',
        'imagen_preview',
        'nombre_archivo',
        'tipo_archivo',
        'tamano_archivo_display',
        'orden',
        'fecha_subida',
    )
    list_filter = (
        'tipo_archivo',
        'fecha_subida',
        'anuncio__activo',
    )
    search_fields = (
        'nombre_archivo',
        'url_imagen',
        'anuncio__titulo',
        'anuncio__modelo',
    )
    readonly_fields = ('id_imagen', 'fecha_subida', 'imagen_preview')
    fieldsets = (
        ('Información Básica', {
            'fields': ('id_imagen', 'anuncio', 'orden')
        }),
        ('Archivo', {
            'fields': ('url_imagen', 'nombre_archivo', 'tipo_archivo', 'tamano_archivo', 'imagen_preview')
        }),
        ('Fechas', {
            'fields': ('fecha_subida',)
        }),
    )
    date_hierarchy = 'fecha_subida'
    list_per_page = 25

    def imagen_preview(self, obj):
        if obj.url_imagen:
            return format_html(
                '<img src="{}" style="max-width: 200px; max-height: 200px;" />',
                obj.url_imagen
            )
        return 'Sin imagen'
    imagen_preview.short_description = 'Vista Previa'

    def tamano_archivo_display(self, obj):
        if obj.tamano_archivo:
            size_kb = obj.tamano_archivo / 1024
            if size_kb < 1024:
                return f'{size_kb:.2f} KB'
            return f'{size_kb / 1024:.2f} MB'
        return 'N/A'
    tamano_archivo_display.short_description = 'Tamaño'


# ==================== NOTIFICACION ADMIN ====================

@admin.register(Notificacion)
class NotificacionAdmin(admin.ModelAdmin):
    """Administración de notificaciones"""
    list_display = (
        'id_notificacion',
        'titulo',
        'tipo',
        'vendedor_display',
        'comprador_display',
        'id_anuncio',
        'leida_display',
        'fecha_creacion',
    )
    list_filter = (
        'tipo',
        'leida',
        'fecha_creacion',
    )
    search_fields = (
        'titulo',
        'mensaje',
        'id_vendedor',
        'id_comprador',
        'nombre_comprador',
        'email_comprador',
        'anuncio__titulo',
    )
    readonly_fields = (
        'id_notificacion',
        'fecha_creacion',
    )
    fieldsets = (
        ('Información Básica', {
            'fields': ('id_notificacion', 'tipo', 'titulo', 'mensaje')
        }),
        ('Usuarios', {
            'fields': ('id_vendedor', 'id_comprador', 'nombre_comprador', 'email_comprador', 'id_usuario')
        }),
        ('Anuncio', {
            'fields': ('id_anuncio',)
        }),
        ('Estado', {
            'fields': ('leida', 'leido', 'fecha_creacion', 'metadata')
        }),
    )
    date_hierarchy = 'fecha_creacion'
    list_per_page = 25
    actions = ['marcar_como_leidas', 'marcar_como_no_leidas']

    def vendedor_display(self, obj):
        if obj.id_vendedor:
            return format_html('<code>{}</code>', obj.id_vendedor[:15] + '...' if len(obj.id_vendedor) > 15 else obj.id_vendedor)
        return '-'
    vendedor_display.short_description = 'Vendedor'

    def comprador_display(self, obj):
        if obj.nombre_comprador:
            return obj.nombre_comprador
        if obj.id_comprador:
            return format_html('<code>{}</code>', obj.id_comprador[:15] + '...' if len(obj.id_comprador) > 15 else obj.id_comprador)
        return '-'
    comprador_display.short_description = 'Comprador'

    def leida_display(self, obj):
        if obj.leida:
            return format_html('<span style="color: green;">✓ Leída</span>')
        return format_html('<span style="color: orange;">○ No leída</span>')
    leida_display.short_description = 'Estado'

    @admin.action(description='Marcar como leídas')
    def marcar_como_leidas(self, request, queryset):
        updated = queryset.update(leida=True, leido=True)
        self.message_user(request, f'{updated} notificación(es) marcada(s) como leída(s).')

    @admin.action(description='Marcar como no leídas')
    def marcar_como_no_leidas(self, request, queryset):
        updated = queryset.update(leida=False, leido=False)
        self.message_user(request, f'{updated} notificación(es) marcada(s) como no leída(s).')


# ==================== HISTORIAL BUSQUEDA ADMIN ====================

@admin.register(HistorialBusqueda)
class HistorialBusquedaAdmin(admin.ModelAdmin):
    """Administración del historial de búsquedas"""
    list_display = (
        'id_historial',
        'placa_consultada',
        'usuario_display',
        'fecha_consulta',
        'tiene_resultado',
    )
    list_filter = (
        'fecha_consulta',
    )
    search_fields = (
        'placa_consultada',
        'id_usuario',
    )
    readonly_fields = (
        'id_historial',
        'fecha_consulta',
        'resultado_api_display',
    )
    fieldsets = (
        ('Información Básica', {
            'fields': ('id_historial', 'id_usuario', 'placa_consultada', 'fecha_consulta')
        }),
        ('Resultado API', {
            'fields': ('resultado_api_display',)
        }),
    )
    date_hierarchy = 'fecha_consulta'
    list_per_page = 25

    def usuario_display(self, obj):
        if obj.id_usuario:
            return format_html('<code>{}</code>', obj.id_usuario[:20] + '...' if len(obj.id_usuario) > 20 else obj.id_usuario)
        return 'Anónimo'
    usuario_display.short_description = 'Usuario'

    def tiene_resultado(self, obj):
        return 'Sí' if obj.resultado_api else 'No'
    tiene_resultado.short_description = 'Tiene Resultado'
    tiene_resultado.boolean = True

    def resultado_api_display(self, obj):
        if obj.resultado_api:
            import json
            return format_html('<pre>{}</pre>', json.dumps(obj.resultado_api, indent=2, ensure_ascii=False))
        return 'Sin resultado'
    resultado_api_display.short_description = 'Resultado API (JSON)'


# ==================== USER ADMIN EXTENDED ====================

class UserAdminExtended(BaseUserAdmin):
    """Extensión del admin de usuarios con información adicional"""
    list_display = BaseUserAdmin.list_display + ('total_anuncios', 'anuncios_activos', 'ultimo_acceso')

    def total_anuncios(self, obj):
        # Contar anuncios por id_usuario (string)
        count = Anuncio.objects.filter(id_usuario=str(obj.id)).count()
        return count
    total_anuncios.short_description = 'Total Anuncios'

    def anuncios_activos(self, obj):
        count = Anuncio.objects.filter(id_usuario=str(obj.id), activo=True).count()
        return format_html('<strong style="color: green;">{}</strong>', count)
    anuncios_activos.short_description = 'Anuncios Activos'

    def ultimo_acceso(self, obj):
        if hasattr(obj, 'last_login') and obj.last_login:
            return obj.last_login
        return 'Nunca'
    ultimo_acceso.short_description = 'Último Acceso'


# Desregistrar el UserAdmin por defecto y registrar el extendido
admin.site.unregister(User)
admin.site.register(User, UserAdminExtended)


# ==================== REPORTES Y ESTADÍSTICAS ====================

# Agregar funcionalidad de reportes a AnuncioAdmin
def get_urls_with_reports(self):
    urls = super(AnuncioAdmin, self).get_urls()
    custom_urls = [
        path('reportes/', self.admin_site.admin_view(self.reportes_view), name='anuncios_reportes'),
    ]
    return custom_urls + urls

def reportes_view(self, request):
    """Vista de reportes y estadísticas"""
    # Estadísticas generales
    total_anuncios = Anuncio.objects.count()
    anuncios_activos = Anuncio.objects.filter(activo=True).count()
    anuncios_inactivos = Anuncio.objects.filter(activo=False).count()
    
    # Estadísticas de precios
    precio_promedio = Anuncio.objects.aggregate(avg=Avg('precio'))['avg'] or 0
    precio_maximo_obj = Anuncio.objects.aggregate(max=Max('precio'))
    precio_maximo = precio_maximo_obj['max'] if precio_maximo_obj['max'] else 0
    precio_minimo = Anuncio.objects.order_by('precio').first()
    precio_minimo_valor = precio_minimo.precio if precio_minimo else 0
    
    # Estadísticas por tipo de vehículo
    por_tipo = Anuncio.objects.values('tipo_vehiculo').annotate(
        total=Count('id_anuncio'),
        precio_promedio=Avg('precio')
    ).order_by('-total')
    
    # Anuncios por mes
    ahora = timezone.now()
    ultimos_6_meses = []
    for i in range(6):
        mes = ahora - timedelta(days=30*i)
        count = Anuncio.objects.filter(
            fecha_creacion__year=mes.year,
            fecha_creacion__month=mes.month
        ).count()
        ultimos_6_meses.append({
            'mes': mes.strftime('%Y-%m'),
            'total': count
        })
    
    # Anuncios recientes
    anuncios_recientes = Anuncio.objects.order_by('-fecha_creacion')[:10]
    
    # Top usuarios por anuncios
    top_usuarios = Anuncio.objects.values('id_usuario').annotate(
        total=Count('id_anuncio')
    ).order_by('-total')[:10]
    
    context = {
        'total_anuncios': total_anuncios,
        'anuncios_activos': anuncios_activos,
        'anuncios_inactivos': anuncios_inactivos,
        'precio_promedio': precio_promedio,
        'precio_maximo': precio_maximo,
        'precio_minimo': precio_minimo_valor,
        'por_tipo': por_tipo,
        'ultimos_6_meses': ultimos_6_meses,
        'anuncios_recientes': anuncios_recientes,
        'top_usuarios': top_usuarios,
    }
    
    return render(request, 'admin/reportes_anuncios.html', context)

# Agregar métodos a AnuncioAdmin
AnuncioAdmin.get_urls = get_urls_with_reports
AnuncioAdmin.reportes_view = reportes_view

# Registrar AnuncioAdmin
admin.site.register(Anuncio, AnuncioAdmin)


# Personalizar el sitio de administración
admin.site.site_header = '🚗 Panel de Administración - checkAuto'
admin.site.site_title = 'checkAuto Admin'
admin.site.index_title = 'Gestión de checkAuto'
