import matplotlib.pyplot as plt
import numpy as np
import subprocess

java_file = "Simulator.java"
num_gens = 50
init_threat = 50
init_coop = 49
init_cheat = 1
arguments = [str(num_gens), str(init_threat), str(init_coop), str(init_cheat)]

compile_process = subprocess.run(['javac', java_file], stdout=subprocess.PIPE, stderr=subprocess.PIPE)

# Check if compilation was successful
if compile_process.returncode == 0:
    # Run the compiled Java program and redirect output to 'output.txt'
    with open('output.txt', 'w') as output_file:
        run_process = subprocess.run(['java', java_file[:-5]] + arguments, stdout=output_file, stderr=subprocess.PIPE)
        if run_process.returncode != 0:
            output_file.write(f"Error running the Java program:\n{run_process.stderr.decode()}")
        output_file.close()
else:
    # If compilation fails, write the error to 'output.txt'
    with open('output.txt', 'w') as output_file:
        output_file.write(f"Compilation Error:\n{compile_process.stderr.decode()}")

with open("output.txt") as f:
    data = f.readline()
    # Get rid of first and last bracket
    data = data[1:-2]
    print(data)
    # Split into individual arrays and clean brackets
    data = data.split("],")
    data[0] = data[0][1:]
    data[1] = data[1][2:]
    data[2] = data[2][2:-1]

    # Convert to nice ararays
    threat_data = []
    for val in data[0].split(","):
        val = int(val.strip())
        threat_data.append(val)
    threat_data = np.array(threat_data)

    coop_data = []
    for val in data[1].split(","):
        val = int(val.strip())
        coop_data.append(val)
    coop_data = np.array(coop_data)

    cheat_data = []
    for val in data[2].split(","):
        val = int(val.strip())
        cheat_data.append(val)
    cheat_data = np.array(cheat_data)
    print(cheat_data)

    time_data = np.arange(len(cheat_data))

    plt.plot(time_data, cheat_data, label="cheater")
    plt.plot(time_data, coop_data, label="cooperator")
    plt.plot(time_data, threat_data, label="threat")
    plt.legend()
    plt.show()
    
