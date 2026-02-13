#!/bin/bash
# Script para compilar y ejecutar ChatFinance sin Maven

echo "═══════════════════════════════════════════════════════"
echo "  ChatFinance - Compilación y Ejecución"
echo "═══════════════════════════════════════════════════════"

# Crear directorios para compilación
mkdir -p out/production/ChatFinance
mkdir -p lib

# Descargar SQLite JDBC si no existe
if [ ! -f "lib/sqlite-jdbc-3.45.0.0.jar" ]; then
    echo "📦 Descargando SQLite JDBC Driver..."
    curl -L "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.0.0/sqlite-jdbc-3.45.0.0.jar" \
         -o "lib/sqlite-jdbc-3.45.0.0.jar"
    echo "✓ SQLite Driver descargado"
fi

# Descargar SLF4J NOP si no existe
if [ ! -f "lib/slf4j-nop-1.7.36.jar" ]; then
    echo "📦 Descargando SLF4J NOP..."
    curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-nop/1.7.36/slf4j-nop-1.7.36.jar" \
         -o "lib/slf4j-nop-1.7.36.jar"
    echo "✓ SLF4J NOP descargado"
fi

# Descargar SLF4J API si no existe
if [ ! -f "lib/slf4j-api-1.7.36.jar" ]; then
    echo "📦 Descargando SLF4J API..."
    curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar" \
         -o "lib/slf4j-api-1.7.36.jar"
    echo "✓ SLF4J API descargado"
fi

# Compilar
echo "🔨 Compilando el proyecto..."
javac -d out/production/ChatFinance \
      -cp "lib/sqlite-jdbc-3.45.0.0.jar:lib/slf4j-api-1.7.36.jar:lib/slf4j-nop-1.7.36.jar" \
      src/Main.java \
      src/modelo/*.java \
      src/dao/*.java \
      src/controller/*.java \
      src/view/*.java \
      src/util/*.java

if [ $? -eq 0 ]; then
    echo "✓ Compilación exitosa"
    echo ""
    echo "🚀 Ejecutando ChatFinance..."
    echo ""

    # Ejecutar
    java -cp "out/production/ChatFinance:lib/sqlite-jdbc-3.45.0.0.jar:lib/slf4j-api-1.7.36.jar:lib/slf4j-nop-1.7.36.jar" Main
else
    echo "✗ Error en la compilación"
    exit 1
fi
