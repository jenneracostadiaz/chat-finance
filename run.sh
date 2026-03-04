#!/bin/bash
# Script para compilar y ejecutar ChatFinance usando Maven

echo "======================================================="
echo "  ChatFinance - Compilacion y Ejecucion"
echo "======================================================="

# Verificar que Maven este instalado
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven no esta instalado. Instala Maven primero."
    exit 1
fi

# Verificar que Java este instalado
if ! command -v java &> /dev/null; then
    echo "ERROR: Java no esta instalado. Instala JDK 17+ primero."
    exit 1
fi

echo "Compilando y empaquetando con Maven..."
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "ERROR: La compilacion fallo. Revisa los errores anteriores."
    exit 1
fi

echo "Compilacion exitosa."
echo "======================================================="
echo ""

# Ejecutar el fat JAR que incluye TODAS las dependencias
java -jar target/ChatFinance-1.0-SNAPSHOT.jar
