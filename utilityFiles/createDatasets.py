import pandas as pd
import os.path
import argparse
from sklearn.model_selection import train_test_split

def checkMethodLength(list, a, b):
    return len(list) >= a and len(list) <= b

parser=argparse.ArgumentParser(
    description='This script generates a dataset to train, validate and test OpenNMT.',
    epilog='Use it responsably!.')
parser.add_argument('--body_switch', help="Switch to apply a filter on the methods' bodies.", type=bool, default=True)
parser.add_argument('--min_body_length', help="Minimum body length to consider.", type=int, default="0")
parser.add_argument('--max_body_length', help="Maximum body length to consider.", type=int, default="200")
parser.add_argument('--training_switch',
                    help="Switch to turn on the functionality to limit the number of items to train the neural model. "
                         "To use together with --training_size",
                    type=bool, default=True)
parser.add_argument('--training_size', help="Number of items to train the neural model. To use together with --training_switch",
                    type=int, default=60000)
parser.add_argument('--validation_size', help="Number of items to validate the neural model.",
                    type=int, default=9000)
parser.add_argument('--testing_percentage', help="Percentage to split the dataset in training and testing datasets.", type= float, default="0.15")

args = parser.parse_args()

body_length_switch = args.body_switch
min_body_length = args.min_body_length
max_body_length = args.max_body_length
training_dataset_fixed_size_switch = args.training_switch
training_dataset_fixed_size = args.training_size
validation_dataset_fixed_size = args.validation_size
testing_dataset_percentage = args.testing_percentage
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
if training_dataset_fixed_size_switch:
    #df_samples = df_pairs.sample(frac=1).reset_index(drop=True).sample(dataset_fixed_size)
    percentage = 1 - (training_dataset_fixed_size / len(df_pairs))
    df_samples, df_valid = train_test_split(df_pairs, test_size=percentage)
    df_valid = df_valid.sample(validation_dataset_fixed_size)
else:
    df_samples = df_pairs

# df_valid = df_pairs.merge(df_samples, indicator=True, how='left')\
#     .query('_merge=="left_only"')\
#     .drop('_merge', axis=1)

df_training, df_testing = train_test_split(df_samples, test_size=testing_dataset_percentage)

df_training['source'].to_csv('./datasets/x_train', sep='\n', header=False, index=False)
df_testing['source'].to_csv('./datasets/x_valid', sep='\n', header=False, index=False)
df_training['target'].to_csv('./datasets/y_train', sep='\n', header=False, index=False)
df_testing['target'].to_csv('./datasets/y_valid', sep='\n', header=False, index=False)

if len(df_valid) > 0:
    df_valid['source'].to_csv('./datasets/remaining_sources', sep='\n', header=False, index=False)
    df_valid['target'].to_csv('./datasets/remaining_references', sep='\n', header=False, index=False)

if process_candidates:
    with open('./datasets/remaining_candidates', 'w') as filehandle:
        filehandle.writelines("%s\n" % place for place in df_valid['candidates'])