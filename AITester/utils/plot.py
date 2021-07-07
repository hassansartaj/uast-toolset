'''
Created on Apr 29, 2020

@author: Hassan Sartaj
@version: 1.0
'''
# import os
import matplotlib
import matplotlib.pyplot as plt
import torch
import numpy as np

# set up matplotlib
is_ipython = 'inline' in matplotlib.get_backend()
if is_ipython:
    from IPython import display

class Plot():
    @staticmethod
    def turnION():
        plt.ion()
    
    @staticmethod
    def plot_values(episode_durations, plot_duration):
        plt.figure(2)
        plt.clf()
        durations_t = torch.tensor(episode_durations, dtype=torch.float)
        plt.title('Training...')
        plt.xlabel('Episode')
        plt.ylabel('Duration')
        plt.plot(durations_t.numpy())
        # Take averages of specified number of episodes and plot them too
        if len(durations_t) >= plot_duration:
            means = durations_t.unfold(dimension=0, size=plot_duration, step=1).mean(1).view(-1)
            means = torch.cat((torch.zeros(plot_duration-1), means))
            plt.plot(means.numpy())
    
        plt.pause(0.001)  # pause a bit so that plots are updated
        if is_ipython:
            display.clear_output(wait=True)
            display.display(plt.gcf())

    @staticmethod
    def plot_rewards(training_rewards, reward_threshold=1000):
        plt.figure(figsize=(12,8))
        plt.plot(training_rewards, label='Rewards')
#         plt.plot(self.mean_training_rewards, label='Mean Rewards')
        plt.xlabel('Episodes')
        plt.ylabel('Rewards')
        plt.ylim([0, np.round(reward_threshold)*1.05])
#         plt.savefig(os.path.join(self.path, 'rewards.png'))
        plt.show()
        
    @staticmethod
    def display_final_plot():   
        plt.ioff()
        plt.show()       