from django.db import models
from django.contrib.auth.models import User
from django.core.validators import MinValueValidator
from decimal import Decimal


class Vehiculo(models.Model):
    """Modelo para almacenar información de vehículos consultados por placa"""
    id_vehiculo = models.AutoField(primary_key=True, db_column='id_vehiculo')
    placa = models.CharField(max_length=20, unique=True, blank=True, null=True, db_column='placa')
    descripcion_api = models.TextField(blank=True, null=True, db_column='descripcion_api')
    marca = models.CharField(max_length=100, blank=True, null=True, db_column='marca')
    modelo = models.CharField(max_length=100, blank=True, null=True, db_column='modelo')
    anio_registro_api = models.CharField(max_length=10, blank=True, null=True, db_column='anio_registro_api')
    vin = models.CharField(max_length=100, blank=True, null=True, db_column='vin')
    uso = models.CharField(max_length=200, blank=True, null=True, db_column='uso')
    propietario = models.TextField(blank=True, null=True, db_column='propietario')
    delivery_point = models.TextField(blank=True, null=True, db_column='delivery_point')
    fecha_registro_api = models.DateTimeField(blank=True, null=True, db_column='fecha_registro_api')
    image_url_api = models.TextField(blank=True, null=True, db_column='image_url_api')
    datos_api = models.JSONField(blank=True, null=True, db_column='datos_api')
    fecha_actualizacion_api = models.DateTimeField(auto_now=True, db_column='fecha_actualizacion_api')
    tipo_vehiculo = models.CharField(max_length=50, blank=True, null=True, db_column='tipo_vehiculo')

    class Meta:
        db_table = 'vehiculos'
        verbose_name = 'Vehículo'
        verbose_name_plural = 'Vehículos'
        ordering = ['-fecha_actualizacion_api']

    def __str__(self):
        return f"{self.placa} - {self.marca} {self.modelo}" if self.marca else self.placa


class CategoriaVehiculo(models.Model):
    """Modelo para categorías de vehículos (permite agregar más dinámicamente)"""
    id_categoria = models.AutoField(primary_key=True, db_column='id_categoria')
    nombre = models.CharField(max_length=100, unique=True, db_column='nombre')
    codigo = models.CharField(max_length=50, unique=True, db_column='codigo')
    descripcion = models.TextField(blank=True, null=True, db_column='descripcion')
    activo = models.BooleanField(default=True, db_column='activo')
    fecha_creacion = models.DateTimeField(auto_now_add=True, db_column='fecha_creacion')
    
    class Meta:
        db_table = 'categorias_vehiculo'
        verbose_name = 'Categoría de Vehículo'
        verbose_name_plural = 'Categorías de Vehículos'
        ordering = ['nombre']
    
    def __str__(self):
        return self.nombre


class Anuncio(models.Model):
    """Modelo para anuncios de vehículos en venta"""

    id_anuncio = models.AutoField(primary_key=True, db_column='id_anuncio')
    id_usuario = models.CharField(max_length=255, db_column='id_usuario')
    titulo = models.CharField(max_length=200, blank=True, null=True, db_column='titulo')
    modelo = models.CharField(max_length=100, db_column='modelo')
    anio = models.IntegerField(validators=[MinValueValidator(1900)], db_column='anio')
    kilometraje = models.IntegerField(validators=[MinValueValidator(0)], db_column='kilometraje')
    precio = models.DecimalField(
        max_digits=12, 
        decimal_places=2, 
        validators=[MinValueValidator(Decimal('0.01'))],
        db_column='precio'
    )
    descripcion = models.TextField(db_column='descripcion')
    email_contacto = models.EmailField(max_length=255, blank=True, null=True, db_column='email_contacto')
    telefono_contacto = models.CharField(max_length=20, blank=True, null=True, db_column='telefono_contacto')
    tipo_vehiculo = models.CharField(
        max_length=50, 
        blank=True, 
        null=True,
        db_column='tipo_vehiculo'
    )
    categoria = models.ForeignKey(
        CategoriaVehiculo,
        on_delete=models.SET_NULL,
        blank=True,
        null=True,
        related_name='anuncios',
        db_column='id_categoria'
    )
    fecha_creacion = models.DateTimeField(auto_now_add=True, db_column='fecha_creacion')
    fecha_actualizacion = models.DateTimeField(auto_now=True, db_column='fecha_actualizacion')
    activo = models.BooleanField(default=True, db_column='activo')

    class Meta:
        db_table = 'anuncios'
        verbose_name = 'Anuncio'
        verbose_name_plural = 'Anuncios'
        ordering = ['-fecha_creacion']

    def __str__(self):
        return f"{self.titulo or self.modelo} - {self.precio}"

    @property
    def total_imagenes(self):
        return self.imagenes.count()


class Imagen(models.Model):
    """Modelo para imágenes de anuncios"""
    id_imagen = models.AutoField(primary_key=True, db_column='id_imagen')
    anuncio = models.ForeignKey(
        Anuncio, 
        on_delete=models.CASCADE, 
        related_name='imagenes',
        db_column='id_anuncio'
    )
    url_imagen = models.TextField(db_column='url_imagen')
    nombre_archivo = models.CharField(max_length=255, blank=True, null=True, db_column='nombre_archivo')
    tipo_archivo = models.CharField(max_length=50, blank=True, null=True, db_column='tipo_archivo')
    tamano_archivo = models.BigIntegerField(blank=True, null=True, db_column='tamano_archivo')
    fecha_subida = models.DateTimeField(auto_now_add=True, db_column='fecha_subida')
    orden = models.IntegerField(default=0, db_column='orden')

    class Meta:
        db_table = 'imagenes'
        verbose_name = 'Imagen'
        verbose_name_plural = 'Imágenes'
        ordering = ['orden', 'fecha_subida']

    def __str__(self):
        return f"Imagen {self.orden} - {self.anuncio}"


class Notificacion(models.Model):
    """Modelo para notificaciones de interés en anuncios"""
    TIPO_CHOICES = [
        ('interes', 'Interés en Anuncio'),
        ('mensaje', 'Mensaje'),
        ('sistema', 'Sistema'),
    ]

    id_notificacion = models.AutoField(primary_key=True, db_column='id_notificacion')
    id_usuario = models.IntegerField(blank=True, null=True, db_column='id_usuario')
    id_vendedor = models.CharField(max_length=255, blank=True, null=True, db_column='id_vendedor')
    id_comprador = models.CharField(max_length=255, blank=True, null=True, db_column='id_comprador')
    nombre_comprador = models.CharField(max_length=255, blank=True, null=True, db_column='nombre_comprador')
    email_comprador = models.EmailField(max_length=255, blank=True, null=True, db_column='email_comprador')
    id_anuncio = models.ForeignKey(
        Anuncio, 
        on_delete=models.SET_NULL, 
        blank=True, 
        null=True,
        db_column='id_anuncio'
    )
    titulo = models.CharField(max_length=200, blank=True, null=True, db_column='titulo')
    mensaje = models.TextField(blank=True, null=True, db_column='mensaje')
    leido = models.BooleanField(default=False, db_column='leido')
    leida = models.BooleanField(default=False, db_column='leida')
    fecha_creacion = models.DateTimeField(auto_now_add=True, db_column='fecha_creacion')
    metadata = models.JSONField(blank=True, null=True, db_column='metadata')
    tipo = models.CharField(
        max_length=50, 
        choices=TIPO_CHOICES, 
        default='interes',
        db_column='tipo'
    )

    class Meta:
        db_table = 'notificaciones'
        verbose_name = 'Notificación'
        verbose_name_plural = 'Notificaciones'
        ordering = ['-fecha_creacion']

    def __str__(self):
        return f"{self.titulo or 'Notificación'} - {self.fecha_creacion}"

    def save(self, *args, **kwargs):
        # Sincronizar campos leido y leida
        if self.leido is not None:
            self.leida = self.leido
        elif self.leida is not None:
            self.leido = self.leida
        super().save(*args, **kwargs)


class HistorialBusqueda(models.Model):
    """Modelo para historial de búsquedas de placas"""
    id_historial = models.AutoField(primary_key=True, db_column='id_historial')
    id_usuario = models.CharField(max_length=255, blank=True, null=True, db_column='id_usuario')
    placa_consultada = models.CharField(max_length=20, blank=True, null=True, db_column='placa_consultada')
    fecha_consulta = models.DateTimeField(auto_now_add=True, db_column='fecha_consulta')
    resultado_api = models.JSONField(blank=True, null=True, db_column='resultado_api')

    class Meta:
        db_table = 'historial_busqueda'
        verbose_name = 'Historial de Búsqueda'
        verbose_name_plural = 'Historial de Búsquedas'
        ordering = ['-fecha_consulta']

    def __str__(self):
        return f"{self.placa_consultada} - {self.fecha_consulta}"
