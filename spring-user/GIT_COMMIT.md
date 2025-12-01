# Cómo hacer commit con un usuario diferente en Git

## Configurar usuario para este repositorio

```bash
# Configurar usuario y email solo para este repositorio
git config user.name "Tu Nombre"
git config user.email "tu.email@ejemplo.com"

# Verificar configuración
git config user.name
git config user.email
```

## Hacer commit de las pruebas

```bash
# Agregar los archivos de prueba
git add src/test/java/com/integrador/service/PlateSearchServiceTest.java

# Hacer commit
git commit -m "test: agregar 3 pruebas unitarias para PlateSearchService"

# Subir al repositorio
git push origin main
```

## Verificar usuario antes de commit

```bash
# Ver configuración actual
git config --list | grep user
```

## Si quieres usar un usuario diferente solo para este commit

```bash
# Hacer commit con usuario específico (sin cambiar configuración)
git -c user.name="Otro Usuario" -c user.email="otro@email.com" commit -m "test: pruebas unitarias"
```


