#!/bin/bash

# Java Version Switcher Script for DevAssist AI
# This script helps switch between different Java versions

useJava8() {
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"
    export PATH="$JAVA_HOME/bin:${PATH}"
    echo "✅ Switched to Java 8"
    java -version
}

useJava11() {
    export JAVA_HOME="/opt/homebrew/opt/openjdk@11"
    export PATH="$JAVA_HOME/bin:${PATH}"
    echo "✅ Switched to Java 11"
    java -version
}

useJava17() {
    export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
    export PATH="$JAVA_HOME/bin:${PATH}"
    echo "✅ Switched to Java 17"
    java -version
}

# Display current Java version
showJava() {
    echo "Current JAVA_HOME: $JAVA_HOME"
    echo "Current Java version:"
    java -version 2>&1 | head -3
}

# Help message
showHelp() {
    echo "Java Version Switcher"
    echo ""
    echo "Usage:"
    echo "  source switch-java.sh"
    echo "  useJava8   - Switch to Java 8"
    echo "  useJava11  - Switch to Java 11"
    echo "  useJava17  - Switch to Java 17 (Recommended for this project)"
    echo "  showJava   - Show current Java version"
    echo ""
    echo "Example:"
    echo "  source switch-java.sh"
    echo "  useJava17"
    echo "  ./gradlew build"
}

# If script is sourced (not executed), show help
if [ "${BASH_SOURCE[0]}" != "${0}" ]; then
    echo "Java switcher functions loaded. Run 'showHelp' for usage."
else
    showHelp
fi


