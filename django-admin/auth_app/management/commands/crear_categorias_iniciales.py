from django.core.management.base import BaseCommand
from auth_app.models import CategoriaVehiculo


class Command(BaseCommand):
    help = 'Crea las categorías iniciales de vehículos'

    def handle(self, *args, **options):
        categorias = [
            {'nombre': 'Sedán', 'codigo': 'sedan', 'descripcion': 'Vehículos sedán de 4 puertas'},
            {'nombre': 'SUV', 'codigo': 'suv', 'descripcion': 'Vehículos deportivos utilitarios'},
            {'nombre': 'Hatchback', 'codigo': 'hatchback', 'descripcion': 'Vehículos hatchback compactos'},
            {'nombre': 'Coupé', 'codigo': 'coupe', 'descripcion': 'Vehículos coupé de 2 puertas'},
            {'nombre': 'Deportivo', 'codigo': 'deportivo', 'descripcion': 'Vehículos deportivos'},
            {'nombre': 'Station Wagon', 'codigo': 'station-wagon', 'descripcion': 'Vehículos familiares tipo wagon'},
            {'nombre': 'Pickup', 'codigo': 'pickup', 'descripcion': 'Camionetas pickup'},
            {'nombre': 'Van', 'codigo': 'van', 'descripcion': 'Vehículos tipo van'},
            {'nombre': 'Motocicleta', 'codigo': 'motocicleta', 'descripcion': 'Motocicletas y scooters'},
        ]
        
        creadas = 0
        for cat_data in categorias:
            categoria, created = CategoriaVehiculo.objects.get_or_create(
                codigo=cat_data['codigo'],
                defaults={
                    'nombre': cat_data['nombre'],
                    'descripcion': cat_data['descripcion'],
                    'activo': True
                }
            )
            if created:
                creadas += 1
                self.stdout.write(
                    self.style.SUCCESS(f'✓ Categoría creada: {categoria.nombre}')
                )
            else:
                self.stdout.write(
                    self.style.WARNING(f'→ Categoría ya existe: {categoria.nombre}')
                )
        
        self.stdout.write(
            self.style.SUCCESS(f'\n✓ Proceso completado. {creadas} categorías nuevas creadas.')
        )

