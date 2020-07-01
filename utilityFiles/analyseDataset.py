import pandas as pd
import seaborn as sns #visualisation
import matplotlib.pyplot as plt #visualisation
import numpy as np
import os.path
sns.set(color_codes=True)

process_candidates = os.path.exists('./datasets/candidates.output')

lowLevelReps = open('./datasets/bytecode.output').readlines()
lowLevelReps = pd.Series([x.rstrip('\n') for x in lowLevelReps])
highLevelReps = open('./datasets/references.output').readlines()
highLevelReps = pd.Series([x.rstrip('\n') for x in highLevelReps])

df_pairs = pd.DataFrame({
        'source': lowLevelReps,
        'source_tokens': [len(x.split()) for x in lowLevelReps],
        'target' : highLevelReps,
        'target_tokens': [len(x.split()) for x in highLevelReps]
    })

if process_candidates:
    candidates = open('./datasets/candidates.output').readlines()
    candidates = [x.rstrip('\n') for x in candidates]
    df_pairs['candidate'] = candidates
    df_pairs['candidate_tokens'] = [len(x.split()) for x in candidates]

print(df_pairs.describe())

for x in range(10, 100, 5):
    q = df_pairs['source_tokens'].quantile(x/100)
    print("Quantile " + str(x) + "%: " + str(q))

mask = df_pairs['source_tokens'] < df_pairs['source_tokens'].quantile(0.95)
print ("Size of the dataset before filtering: " + str(len(df_pairs)))
df_pairs = df_pairs.loc[mask]
print ("Size of the dataset after filtering: " + str(len(df_pairs)))


#sns.boxplot(x=df_pairs['source_tokens'])
#sns.boxplot(x=df_pairs['target_tokens'])
sns.distplot( df_pairs['source_tokens'], bins=3 )
plt.show()