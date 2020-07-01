import pandas as pd
import os.path
import math
from sklearn.model_selection import train_test_split

def checkMethodLength(list, a, b):
    return len(list) >= a and len(list) <= b

body_length_switch = True
min_body_length = 89
max_body_length = math.inf
dataset_fixed_size_switch = True
dataset_fixed_size = 50000
process_candidates = os.path.exists('./datasets/candidates.output')

lowLevel = open('./datasets/bytecode.output').readlines()
lowLevel = [x.rstrip('\n') for x in lowLevel]
highLevel = open('./datasets/references.output').readlines()
highLevel = [x.rstrip('\n') for x in highLevel]

if process_candidates:
    candidates = open('./datasets/candidates.output').readlines()
    candidates = [x.rstrip('\n') for x in candidates]
    df_pairs = pd.DataFrame({'source': lowLevel, 'target' : highLevel, 'candidates': candidates })
else:
    df_pairs = pd.DataFrame({'source': lowLevel, 'target' : highLevel})

if body_length_switch:
    mask = df_pairs['source'].apply(lambda x: checkMethodLength(x.split(), min_body_length, max_body_length))
    df_pairs = df_pairs.loc[mask]

# Here we want to throw an exception when the size of the dataset is smaller than the
# variable dataset_fixed_size. This would highlight that there is something wrong
if dataset_fixed_size_switch:
    df_samples = df_pairs.sample(frac=1).reset_index(drop=True).sample(dataset_fixed_size)
else:
    df_samples = df_pairs

df_valid = df_pairs.merge(df_samples, indicator=True, how='left')\
    .query('_merge=="left_only"')\
    .drop('_merge', axis=1)

lowLevel, x_test, highLevel, y_test = train_test_split(df_samples.source, df_samples.target, test_size=0.05)

lowLevel.to_csv('./datasets/x_train', sep='\n', header=False, index=False)
x_test.to_csv('./datasets/x_valid', sep='\n', header=False, index=False)
highLevel.to_csv('./datasets/y_train', sep='\n', header=False, index=False)
y_test.to_csv('./datasets/y_valid', sep='\n', header=False, index=False)

df_valid['source'].to_csv('./datasets/remaining_sources', sep='\n', header=False, index=False)
df_valid['target'].to_csv('./datasets/remaining_references', sep='\n', header=False, index=False)

if process_candidates:
    with open('./datasets/remaining_candidates', 'w') as filehandle:
        filehandle.writelines("%s\n" % place for place in df_valid['candidates'])