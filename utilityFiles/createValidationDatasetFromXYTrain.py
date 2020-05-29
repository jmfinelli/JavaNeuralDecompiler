import pandas as pd

x_train = open('./datasets/x_train').readlines()
x_train = [x.rstrip('\n') for x in x_train]
y_train = open('./datasets/y_train').readlines()
y_train = [x.rstrip('\n') for x in y_train]

x_valid = open('./datasets/x_valid').readlines()
x_valid = [x.rstrip('\n') for x in x_valid]
y_valid = open('./datasets/y_valid').readlines()
y_valid = [x.rstrip('\n') for x in y_valid]

df_pairs = pd.read_csv('./datasets/pairs.output', sep='\t', header=None, names=['source', 'target'])

df_train = pd.DataFrame({'source': x_train + x_valid, 'target' : y_train + y_valid })

df_valid = df_pairs.merge(df_train, on='source', indicator=True, how='left')\
    .query('_merge=="left_only"')\
    .drop('_merge', axis=1)\
    .drop('target_y', axis=1)

df_valid = df_valid.sample(frac=1).reset_index(drop=True).sample(50000)

with open('./datasets/remaining_sources', 'w') as filehandle:
    filehandle.writelines("%s\n" % place for place in df_valid['source'])

with open('./datasets/remaining_references', 'w') as filehandle:
    filehandle.writelines("%s\n" % place for place in df_valid['target_x'])