import matplotlib.pyplot as plt
from matplotlib.patches import Patch
import numpy as np
import subprocess

java_file = "Simulator.java"

compile_process = subprocess.run(['javac', java_file], stdout=subprocess.PIPE, stderr=subprocess.PIPE)


def simulate(output_id, threat, coop, cheat, coop_threshold):
    games_per_gen = 10
    arguments = [str(games_per_gen), str(threat), str(coop), str(cheat), str(coop_threshold)]
    output_str = f"output_{output_id}.txt"
    with open(output_str, 'w') as output_file:
        run_process = subprocess.run(['java', java_file[:-5]] + arguments, stdout=output_file, stderr=subprocess.PIPE)
        if run_process.returncode != 0:
            output_file.write(f"Error running the Java program:\n{run_process.stderr.decode()}")
        output_file.close()

def plot_data(output_ids):

    num_rows = len(output_ids) + 1 // 3
    num_cols = 3

    fig, axes = plt.subplots(num_rows, num_cols, figsize=(10, 2.5 * num_rows))
    axes = axes.flatten()

    for i, output_id in enumerate(output_ids):
        output_str = f"output_{output_id}.txt"
        with open(output_str) as f:
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

            ax = axes[i]

            ax.plot(time_data, cheat_data, label="cheater")
            ax.plot(time_data, coop_data, label="cooperator")
            ax.plot(time_data, threat_data, label="threat")
            ax.set_xlabel("Generation #")
            ax.set_ylabel("Population size")
            ax.set_title(f"Threshold = {output_id}")
    
    for j in range(len(output_ids), num_rows * num_cols):
        fig.delaxes(axes[j])

    legend_els = [
        Patch(facecolor='blue', label='cheater'),
        Patch(facecolor='orange', label='cooperator'),
        Patch(facecolor='green', label='threat')
    ]

    fig.legend(handles=legend_els, loc='upper right')

    plt.tight_layout()
    plt.show()


# Check if compilation was successful
if compile_process.returncode == 0:
    # Run the compiled Java program and redirect output to 'output.txt'
    for i in range(1, 10):
        simulate(i / 10.0, 33, 33, 34, i / 10.0)

    plot_data([i / 10.0 for i in range(1, 10)])
else:
    # If compilation fails, write the error to 'output.txt'
    with open('output.txt', 'w') as f:
        f.write(f"Compilation Error:\n{compile_process.stderr.decode()}")


    
