# Cómo hacer 3 commits con 3 usuarios diferentes

## Ejecutar las pruebas primero

```bash
# Verificar que las 3 pruebas pasan
mvn test -Dtest=PlateSearchServiceTest1
mvn test -Dtest=PlateSearchServiceTest2
mvn test -Dtest=PlateSearchServiceTest3
```

## Paso 1: Commit de la primera prueba (Usuario 1)

```bash
# Configurar usuario 1
git config user.name "Usuario 1"
git config user.email "usuario1@ejemplo.com"

# Agregar primera prueba
git add src/test/java/com/integrador/service/PlateSearchServiceTest1.java

# Hacer commit
git commit -m "test: agregar prueba 1 - búsqueda exitosa de placa"

# Verificar usuario del commit
git log -1 --format="%an <%ae>"
```

## Paso 2: Commit de la segunda prueba (Usuario 2)

```bash
# Configurar usuario 2
git config user.name "Usuario 2"
git config user.email "usuario2@ejemplo.com"

# Agregar segunda prueba
git add src/test/java/com/integrador/service/PlateSearchServiceTest2.java

# Hacer commit
git commit -m "test: agregar prueba 2 - manejo de respuesta vacía"

# Verificar usuario del commit
git log -1 --format="%an <%ae>"
```

## Paso 3: Commit de la tercera prueba (Usuario 3)

```bash
# Configurar usuario 3
git config user.name "Usuario 3"
git config user.email "usuario3@ejemplo.com"

# Agregar tercera prueba
git add src/test/java/com/integrador/service/PlateSearchServiceTest3.java

# Hacer commit
git commit -m "test: agregar prueba 3 - manejo de excepción de API"

# Verificar usuario del commit
git log -1 --format="%an <%ae>"
```

## Subir todos los commits al repositorio

```bash
git push origin main
```

## Verificar los 3 commits con sus usuarios

```bash
# Ver los últimos 3 commits con sus usuarios
git log -3 --format="%h - %an <%ae> : %s"
```

## Ejemplo completo con usuarios reales

Reemplaza los nombres y emails con los que necesites:

```bash
# Usuario 1 - Prueba 1
git config user.name "Juan Pérez"
git config user.email "juan.perez@ejemplo.com"
git add src/test/java/com/integrador/service/PlateSearchServiceTest1.java
git commit -m "test: agregar prueba 1 - búsqueda exitosa de placa"

# Usuario 2 - Prueba 2
git config user.name "María García"
git config user.email "maria.garcia@ejemplo.com"
git add src/test/java/com/integrador/service/PlateSearchServiceTest2.java
git commit -m "test: agregar prueba 2 - manejo de respuesta vacía"

# Usuario 3 - Prueba 3
git config user.name "Carlos López"
git config user.email "carlos.lopez@ejemplo.com"
git add src/test/java/com/integrador/service/PlateSearchServiceTest3.java
git commit -m "test: agregar prueba 3 - manejo de excepción de API"

# Subir todo
git push origin main
```

