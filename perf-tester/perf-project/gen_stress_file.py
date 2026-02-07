
import os

filename = "concord.yaml"

with open(filename, "w") as f:
    f.write("resources:\n")
    f.write("  concord:\n")
    f.write("    - \"glob:concord/{**/,}{*.,}concord.yaml\"\n")
    f.write("    - \"glob:flows/{**/,}{*.,}concord.yaml\"\n")
    f.write("    - \"glob:profiles/{**/,}{*.,}concord.yaml\"\n")
    f.write("    - \"glob:triggers/{**/,}{*.,}concord.yaml\"\n")
    f.write("\n")
    f.write("configuration:\n")
    f.write("  runtime: concord-v2\n")
    f.write("  arguments:\n")
    
    # Генерируем 1000 аргументов (definitions)
    for i in range(1000):
        f.write(f"    stress_arg_{i}: \"value_{i}\"\n")
        
    f.write("\nflows:\n")
    f.write("  default:\n")
    f.write("    - log: \"Start\"\n")
    f.write("    - call: stress_flow\n")
    
    f.write("  stress_flow:\n")
    
    # Генерируем 1000 шагов, использующих эти аргументы (references + injections)
    for i in range(1000):
        if i == 500:
            f.write("    # move here\n")
        f.write(f"    - log: \"Step {i} processing ${{stress_arg_{i}}}\"\n")
        f.write(f"    - if: \"${{stress_arg_{i} == 'test'}}\"\n")
        f.write("      then:\n")
        f.write("        - set:\n")
        f.write(f"            temp_var_{i}: \"${{stress_arg_{i}}}\"\n")
        
    f.write("    - log: \"Done\"\n")

print(f"Generated {filename} with heavy content.")
