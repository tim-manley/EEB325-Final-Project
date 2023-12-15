import matplotlib.pyplot as plt
import numpy as np

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
    
