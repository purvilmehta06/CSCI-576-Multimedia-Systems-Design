import os
import pandas as pd
import matplotlib.pyplot as plt

folder_path = 'results'
csv_files = [f for f in os.listdir(folder_path) if f.endswith('.csv')]

# Loop through each CSV file and plot a graph
plt.figure() 
for csv_file in csv_files:
    file_path = os.path.join(folder_path, csv_file)
    data = pd.read_csv(file_path)
    x = data['X']
    y = data['Y']
    plt.plot(x, y, label = csv_file.replace('.csv', ''))
    
# Show all the plots
plt.xlabel('Percentage of noise added')
plt.ylabel('Reconstructed image error')
plt.legend()
plt.grid()
plt.savefig('results.png')
plt.show()