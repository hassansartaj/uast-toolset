'''
Created on Apr 29, 2020

@author: Hassan Sartaj
@version: 1.0
'''

import sys
import torch
import torch.cuda
from drl.aitester import EpsilonGreedyStrategy, AITester
from drl.uavenv import UAVCopterEnvManager

#----------------#
#Hyper parameters#
#----------------#
# BATCH_SIZE = 128
# BATCH_SIZE = 128
# GAMMA = 0.999
# EPS_START = 0.9
# EPS_END = 0.05  #0.01
# EPS_DECAY = 200
# TARGET_UPDATE = 10
# MEMORY_SIZE = 1000 #10000 - std
# LEARNING_RATE = 0.001
# NUM_EPISODES = 1000

#----------------#
#New - Hyper parameters#
#----------------#
BATCH_SIZE = 128
GAMMA = 0.999
EPS_START = 1
EPS_END = 0.01  #0.01
EPS_DECAY = 100
TARGET_UPDATE = 10
MEMORY_SIZE = 1024 #10000 - std
LEARNING_RATE = 0.001
NUM_EPISODES = 5000

if __name__ == '__main__':
    
    if len(sys.argv) < 3:
        print('The following two arguments are required. \n argv[1] = mode (train/eval) \n argv[2] = model path')
        sys.exit(0)
    mode = sys.argv[1]
    if mode != 'train' and mode != 'eval':
        print('The first argument must be train or eval')
        sys.exit(0)

    model_path = sys.argv[2]

    device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
    print("Running on device:: ", device)
    env_manager = UAVCopterEnvManager(device)
    env_manager.reset()

    show_plot=False
    strategy = EpsilonGreedyStrategy(EPS_START, EPS_END, EPS_DECAY)
    agent = AITester(strategy, env_manager, MEMORY_SIZE, LEARNING_RATE, device, show_plot)    

    if mode == 'train':
        agent.train(NUM_EPISODES, BATCH_SIZE, GAMMA, TARGET_UPDATE, model_path, restore=False)
    elif mode == 'eval':
        agent.evaluate(NUM_EPISODES , model_path)
