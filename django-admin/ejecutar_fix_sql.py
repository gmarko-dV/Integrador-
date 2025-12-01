import psycopg2
from django.conf import settings
import os
import django

# Configurar Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'admin_backend.settings')
django.setup()

# Leer el archivo SQL
with open('fix_categorias_table.sql', 'r', encoding='utf-8') as f:
    sql_script = f.read()

# Conectar a la base de datos
try:
    conn = psycopg2.connect(
        host='localhost',
        port=5432,
        database='car_Sales_pe',
        user='postgres',
        password='123456'
    )
    conn.autocommit = True
    cursor = conn.cursor()
    
    # Ejecutar el script SQL
    cursor.execute(sql_script)
    
    print("✓ Script SQL ejecutado exitosamente")
    print("✓ Columnas agregadas a la tabla categorias_vehiculo")
    
    # Verificar las columnas
    cursor.execute("""
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns
        WHERE table_name = 'categorias_vehiculo'
        ORDER BY ordinal_position;
    """)
    
    print("\nColumnas actuales en categorias_vehiculo:")
    for row in cursor.fetchall():
        print(f"  - {row[0]}: {row[1]} (nullable: {row[2]})")
    
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"Error al ejecutar el script: {e}")
    import traceback
    traceback.print_exc()

