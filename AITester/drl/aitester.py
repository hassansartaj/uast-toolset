'''
Created on Apr 29, 2020

@author: Hassan Sartaj
@version: 1.0
'''

import os
import math
import random
import datetime
import torch
import torch.cuda
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
from itertools import count
import numpy as np
import time
from utils import UAVModel
from utils.plot import Plot
from torch.utils.tensorboard import SummaryWriter
writer = SummaryWriter()

# set constant random seed
torch.manual_seed(1)
np.random.seed(1)
random.seed=1


class AITester():
    def __init__(self, strategy, env_manager, memory_size, learning_rate, device, show_plot,algo):
        self.algo = algo
        self.current_step = 0
        self.strategy = strategy
        self.env_manager = env_manager
#         self.state_space = env_manager.get_state_space()
        self.action_space = env_manager.get_action_space()
        self.num_actions = env_manager.get_total_actions()
        self.num_states = env_manager.get_total_states()  
        self.device = device
        
        self.policy_net = ANN(self.num_states, self.num_actions).to(self.device)
        self.target_net = ANN(self.num_states, self.num_actions).to(self.device)
        
#         self.learn_step_counter = 0
        self.memory_size = memory_size
        self.memory_counter = 0
#         self.memory = np.zeros((memory_size, self.num_states + 3))
        self.memory = np.zeros((memory_size, self.num_states*2 + 2))
        self.optimizer = optim.Adam(self.policy_net.parameters(), lr=learning_rate)
#         self.loss_func = nn.MSELoss() 
        self.loss_func = nn.SmoothL1Loss()
        
        self.predicted_counter=0
        self.is_predicted = False
        self.random_counter=0
        self.show_plot=show_plot

    
    def store_experience(self, state, action, reward, next_state):
        experience = np.hstack((state, [action, reward], next_state))
        index = self.memory_counter % self.memory_size
        self.memory[index, :] = experience
        self.memory_counter += 1


    def select_action(self, state):
        if self.algo=="AITester":
            rate = self.strategy.get_exploration_rate(self.current_step)
            self.current_step += 1
            if rate <= np.random.randn():   # greedy policy - exploit
                with torch.set_grad_enabled(False):   # turn off gradient tracking
                    action_value = self.policy_net.forward(state.float().to(self.device))
                    # action = torch.max(action_value, 0)[1].data.numpy()  # for cpu only
                    # action = torch.max(action_value, 0)[1].cpu().data.numpy()
                    # for lstm
                    action = torch.max(action_value, 1)[1].cpu().data.numpy()
                    # print("action: ", torch.max(action_value, 1)[1].cpu().data.numpy())
                self.predicted_counter+=1
                self.is_predicted=True
            else:    # random policy  - explore
                with torch.set_grad_enabled(False):  # turn off gradient tracking
                    # print("state[0]: ", state[0][0][0].item())
                    # actions = self.env_manager.get_available_actions(state[0].item())  #select an action w.r.t. the given state
                    # for lstm
                    actions = self.env_manager.get_available_actions(
                        state[0][0][0].item())  # select an action w.r.t. the given state
                    index = random.randrange(len(actions))
                    action = actions[index]
                    self.random_counter+=1
            return action
        else:
            with torch.set_grad_enabled(False):  # turn off gradient tracking
                # print("state[0]: ", state[0][0][0].item())
                # actions = self.env_manager.get_available_actions(state[0].item())  #select an action w.r.t. the given state
                # for lstm
                actions = self.env_manager.get_available_actions(
                    state[0][0][0].item())  # select an action w.r.t. the given state
                index = random.randrange(len(actions))
                action = actions[index]
                self.random_counter += 1
            return action


    def select_action_eval(self, state):
        # with torch.set_grad_enabled(False):   # turn off gradient tracking
        action_value = self.policy_net.forward(state.float().to(self.device))
                # action = torch.max(action_value, 0)[1].data.numpy()  # for cpu only
        
        action = torch.max(action_value, 0)[1].cpu().data.numpy()
        self.predicted_counter+=1
            # self.is_predicted=True
        return action
    
    def loadANNs(self, model_path=None):
        # load saved model from model path 
        if model_path is not None:
            policy_state_dict = torch.load(model_path+'/policynet.model')
            target_state_dict = torch.load(model_path+'/targetnet.model')
            self.policy_net.load_state_dict(policy_state_dict)
            self.target_net.load_state_dict(target_state_dict)

    def save_models(self, model_path):
        #save both trained models
        if not os.path.exists(model_path):
            os.mkdir(model_path)
            print("Directory " , model_path ,  " created.")
        torch.save(self.policy_net.state_dict(), model_path+'/policynet.model')
        torch.save(self.target_net.state_dict(), model_path+'/targetnet.model')
        print('Trained models (policy and target) are saved to ', model_path)
        
    def load_training_state(self, model_path):  
        cpfile = open(model_path+'/checkpoint.txt', 'r')
        line = cpfile.readline()
        ep_start = int(line.split(' ')[1])
        line = cpfile.readline()
        incorrect_counter = int(line.split(' ')[1])
        line = cpfile.readline()
        self.predicted_counter = int(line.split(' ')[1])
        line = cpfile.readline()
        action_counter = int(line.split(' ')[1])
        # line = cpfile.readline()
        # self.memory_counter = int(line.split(' ')[1])
        return ep_start, incorrect_counter, action_counter

    def get_results_file(self, dir_name, file_name):
        if not os.path.exists(dir_name):
            os.mkdir(dir_name)
            print("Directory " , dir_name ,  " created.")
        resfile = open(dir_name + '/'+file_name+'.txt', 'a+')
        resfile.write("Episode,CorrectActs,IncorrectActs,RandomActs,Accuracy,Reward,Loss,Time(s)\n")
        resfile.flush()
        return resfile
    #---------------------#
    # Main training phase #
    #---------------------#
    def train(self, num_episodes, batch_size, gamma, target_update, model_path, restore=False):
        Plot.turnION()
        incorrect_counter=0
        action_counter=0
        ep_start=1
        resfile = self.get_results_file(dir_name="results", file_name="training-results")
        #load incomplete training state
        if restore:
            self.loadANNs(model_path)
            ep_start, incorrect_counter, action_counter = self.load_training_state(model_path)
        # else:
        #     resfile.write("Episode \t CorrectActs \t IncorrectActs \t RandomActs \t Accuracy \t Reward \t Loss \t Time(s)")


        # set model to training mode
        self.policy_net.train()
        #training loop
        episode_durations=[]
        with open("episode_action_ocl.csv","a+") as acOCL, open("databins.csv","a+") as databins:
            acOCL.write("Episode,Action,Failed,Passed\n")
            for episode in range(ep_start, num_episodes+1):
                print("Episode # ", episode, " - memory_counter: ", self.memory_counter, "\n")
                loss_res = 0
                start_time = datetime.datetime.now()
                # Initialize the environment and state
                self.env_manager.reset()
                state = self.env_manager.get_state()
                ep_reward=0

                for timestamp in count():
                    # Select and perform an action
                    state_n = state.view(-1, 1, 10).requires_grad_()
                    data = state.cpu().detach().numpy()
                    databins.write(",".join(data.astype("str"))+","+str(time.time())+"\n")
                    databins.flush()
                    self.policy_net.init_hiddenState(1)
                    action = self.select_action(state_n)
                    # Get the reward
                    reward,failed = self.env_manager.take_action(action)
                    if type(action) is np.ndarray:
                        action_num = action.item()
                    else:
                        action_num = action
                    acOCL.write(str(episode)+","+UAVModel.getActionName(action_num)+","+failed["failed"]+","+failed["passed"]+"\n")
                    acOCL.flush()
                    # Observe new state
                    next_state = self.env_manager.get_state()
                    # Store the experience in memory
    #                 self.store_experience(self.encode(state), action, reward, next_state)
                    self.store_experience(state, action, reward, next_state)
                    # Move to the next state
                    state = next_state
                    #Check for the negative reward w.r.t. the selected action
                    action_counter+=1
                    if reward[0] < 0 and self.is_predicted:
                        incorrect_counter+=1
                    self.is_predicted=False
                    ep_reward+=reward[0].item()
    #                 print("- State={}, Action={}, Reward={}, NextState={}".format(state, action, reward, next_state))

                    if self.memory_counter >= self.memory_size:
                        # Update the parameters (copy all weights and biases in target_net)
                        if episode%target_update == 0:
                            self.target_net.load_state_dict(self.policy_net.state_dict())

                        # Alternative for updating the parameters
    #                     if self.learn_step_counter % num_episodes ==0:
    #                         target_net.load_state_dict(policy_net.state_dict())
    #                     self.learn_step_counter+=1

                        # Sample batch from memory
                        sample_index = np.random.choice(self.memory_size, batch_size)
                        batch_memory = self.memory[sample_index, :]

                        # batch_state = torch.FloatTensor(batch_memory[:, :self.num_states])
                        # batch_action = torch.LongTensor(batch_memory[:, self.num_states:self.num_states + 1].astype(int))
                        # batch_reward = torch.FloatTensor(batch_memory[:, self.num_states + 1:self.num_states + 2])
                        # batch_next_state = torch.FloatTensor(batch_memory[:, -self.num_states:])

                        batch_state = torch.cuda.FloatTensor(batch_memory[:, :self.num_states])
                        batch_action = torch.cuda.LongTensor(batch_memory[:, self.num_states:self.num_states+1].astype(int))
                        batch_reward = torch.cuda.FloatTensor(batch_memory[:, self.num_states+1:self.num_states+2])
                        batch_next_state = torch.cuda.FloatTensor(batch_memory[:,-self.num_states:])

                        # Calculate Q values
                        # q_eval = self.policy_net(batch_state).gather(1, batch_action)
                        # for lstm
                        batch_state_n = batch_state.view(-1, 1, 10).requires_grad_()
                        #for lstm
                        self.policy_net.init_hiddenState(batch_size)
                        #-=-=-=-=
                        q_eval = self.policy_net(batch_state_n).gather(1, batch_action)
                        # for lstm
                        batch_next_state_n = batch_next_state.view(-1, 1, 10).requires_grad_()
                        #for lstm
                        self.target_net.init_hiddenState(batch_size)
                        #=-=-=-=-
                        q_next = self.target_net(batch_next_state_n).detach()
                        q_target = batch_reward + gamma * q_next.max(1)[0].view(batch_size, 1)
                        # Compute loss using Q and target networks
                        loss = self.loss_func(q_eval, q_target)
                        loss_res = loss.item() #.cpu().data.numpy()
                        # Set gradient to zero before back propagation
                        self.optimizer.zero_grad()
                        # Back propagate the loss
                        loss.backward()
                        # Minimize the loss
                        self.optimizer.step()

                        writer.add_scalar('Loss/train', loss, episode)

                    if self.env_manager.done:
    #                     print("[Done]- State={}, Action={}, Reward={}, NextState={}".format(state, action, reward, next_state))
                        episode_durations.append(timestamp)
                        if self.show_plot:
                            Plot.plot_values(episode_durations, 100)
                        break

                end_time = datetime.datetime.now()
                elapsed_time = end_time - start_time

                # log training accuracy
                if self.predicted_counter > 0:
                    # acc = ((self.predicted_counter-incorrect_counter)/self.predicted_counter)*100
                    acc = (self.predicted_counter / (self.predicted_counter + incorrect_counter)) * 100
                    print("\n==> incorrect actions: ", incorrect_counter)
                    print("--> predicted_counter: ", self.predicted_counter)
                    print("--> random_counter: ", self.random_counter)
                    print("Accuracy:: ", acc)
                    # average_acc = round(acc / episode)
                    writer.add_scalar('Accuracy/train', acc, episode)
                    # writer.add_scalar('AverageAccuracy/train', average_acc, episode)
                    resfile.write(str(episode) + "," + str(self.predicted_counter) + "," + str(incorrect_counter) + "," + str(self.random_counter)
                                  + "," + str(acc) + "," + str(ep_reward) + "," + str(loss_res) + "," + str(elapsed_time.seconds)+"\n")
                    resfile.flush()
                    self.predicted_counter = 0
                    incorrect_counter = 0
                    self.random_counter = 0
                writer.flush()
                # log episode duration
                writer.add_scalar('Duration(s)/train', elapsed_time.seconds, episode)

                print("Reward : ", ep_reward, "\n")
                # log reward
                writer.add_scalar('Reward/train', ep_reward, episode)
    #             if ((self.predicted_counter-incorrect_counter)/self.predicted_counter)*100 >= 95.0:
    #                 print("==> %age accuracy is > 95 ")
    #                 break

                # Save models after a specified number of episodes
                if episode%10 == 0:
                    self.save_models(model_path)
                    cpfile = open(model_path+'/checkpoint.txt', 'w')
                    cpfile.write("Episode "+str(episode)+"\n"+"IC "+str(incorrect_counter)+"\n"+
                                 "PC "+str(self.predicted_counter)+"\n"+"AC "+str(action_counter))
                    cpfile.close()
                self.env_manager.reset_uav()

            self.env_manager.close()
    #         Plot.display_final_plot()

            # print("\n==> incorrect actions: ", incorrect_counter, " - out of total actions: ", action_counter)
            # print("--> predicted_counter: ", self.predicted_counter)
            # print("--> random_counter: ", self.random_counter)
            # print("==> %age accuracy: ", (float(self.predicted_counter-incorrect_counter)/self.predicted_counter)*100)

            writer.close()
            print("\nTraining completed. Saving trained models to "+model_path+" ...")
    #         #save both trained models
    #         if not os.path.exists(model_path):
    #             os.mkdir(model_path)
    #             print("Directory " , model_path ,  " created.")
    #
    #         torch.save(self.policy_net.state_dict(), model_path+'/policynet.model')
    #         torch.save(self.target_net.state_dict(), model_path+'/targetnet.model')
    #         print('Trained models (policy and target) are saved to ', model_path)
        resfile.close()
    
    #------------------#
    # Evaluation phase #
    #------------------#
    def evaluate(self, num_episodes, model_path):
        resfile = self.get_results_file(dir_name="results", file_name="eval-results")
        # resfile.write("Episode \t CorrectActs \t IncorrectActs \t RandomActs \t Accuracy \t Reward \t Loss \t Time(s)")
        # set model to evaluation mode
        self.policy_net.eval()
        self.loadANNs(model_path)
        incorrect_counter=0
        action_counter=0
        episode_durations=[]
        for episode in range(num_episodes):
            print("Episode # ", episode, "\n")
            loss_res=0
            start_time = datetime.datetime.now()
            # Initialize the environment and state
            self.env_manager.reset()
            state = self.env_manager.get_state()
            ep_reward=0
            for timestamp in count():
                # Select and perform an action
                action = self.select_action(state)
                action = self.select_action_eval(state)
                # Get the reward
                reward = self.env_manager.take_action(action)
                # Observe new state
                next_state = self.env_manager.get_state()
                # Move to the next state
                state = next_state
                #Check for the negative reward w.r.t. the selected action
                action_counter += 1
                if reward[0] == -1:
                    incorrect_counter+=1
                    # break
                    
                ep_reward += reward[0].item()
                    
                if self.env_manager.done:
                    episode_durations.append(timestamp)
#                     Plot.plot_values(episode_durations, 10)
                    break

            end_time = datetime.datetime.now()
            elapsed_time = end_time - start_time

            resfile.write(str(episode) + "," + str(self.predicted_counter) + "," + str(incorrect_counter) + "," + str(self.random_counter)
                + "," + str(acc) + "," + str(ep_reward) + "," + str(loss_res) + "," + str(elapsed_time.seconds)+"\n")
            resfile.flush()
            self.env_manager.reset_uav()
            # log episode duration
            writer.add_scalar('Duration(s)/train', elapsed_time.seconds, episode)
            print("Reward : ", ep_reward, "\n")
            # log reward
            writer.add_scalar('Reward/train', ep_reward, episode)
            acc = ((self.predicted_counter - incorrect_counter) / self.predicted_counter) * 100
            print("\n==> incorrect actions: ", incorrect_counter)
            print("--> predicted_counter: ", self.predicted_counter)
            print("Accuracy:: ", acc)
            # average_acc = round(((average_acc + acc) / episode) * 100)
            # log accuracy
            writer.add_scalar('Accuracy/train', acc, episode)
            # writer.add_scalar('AverageAccuracy/train', average_acc, episode)
        resfile.close()
        # print("\n==> incorrect actions: ", incorrect_counter, " - out of total actions: ", action_counter)
        # print("--> predicted_counter: ", self.predicted_counter)
        # print("--> random_counter: ", self.random_counter)
        # print("==> %age accuracy: ", ((self.predicted_counter-incorrect_counter)/self.predicted_counter)*100)

# TODO: log the NN input data
class ANN(nn.Module):
    def __init__(self, inputs, outputs):
        super().__init__();

#         -----------------------------------------------------         #
#                      LSTM
#         -----------------------------------------------------         #
        hidden_dim = 10
        layer_dim = 3
        self.cn = 0
        self.hn = 0
        # Hidden dimensions
        self.hidden_dim = hidden_dim

        # Number of hidden layers
        self.layer_dim = layer_dim

        # Building your LSTM
        # batch_first=True causes input/output tensors to be of shape
        # (batch_dim, seq_dim, feature_dim)
        self.lstm = nn.LSTM(inputs, hidden_dim, layer_dim, batch_first=True)

        # Readout layer
        self.fc = nn.Linear(hidden_dim, outputs)
    

    def init_hiddenState(self, bs):
        """Initialize the hidden state of RNN to all zeros
        Args:
            bs ([int]): [Batch size during training]
        """
        self.cn = torch.zeros(self.layer_dim, bs, self.hidden_dim).requires_grad_().to(device="cuda:0")
        self.hn = torch.zeros(self.layer_dim, bs, self.hidden_dim).requires_grad_().to(device="cuda:0")

    def forward(self, x):


#         -----------------------------------------------------         #
#                      LSTM
#         -----------------------------------------------------         #

        # Initialize hidden state with zeros
        #h0 = torch.zeros(self.layer_dim, x.size(0), self.hidden_dim).requires_grad_().to(device="cuda:0")

        # Initialize cell state
        #c0 = torch.zeros(self.layer_dim, x.size(0), self.hidden_dim).requires_grad_().to(device="cuda:0")

        # 28 time steps
        # We need to detach as we are doing truncated backpropagation through time (BPTT)
        # If we don't, we'll backprop all the way to the start even after going through another batch
        out, (self.hn, self.cn) = self.lstm(x, (self.hn.detach(), self.cn.detach()))

        # Index hidden state of last time step
        # out.size() --> 100, 28, 100
        # out[:, -1, :] --> 100, 100 --> just want last time step hidden states!
        out = self.fc(out[:, -1, :])
        # out.size() --> 100, 10
        return out


class EpsilonGreedyStrategy():
    def __init__(self, start, end, decay):
        self.start = start
        self.end = end
        self.decay = decay
    
    def get_exploration_rate(self, current_step):
        return self.end + (self.start - self.end) * \
        math.exp(-1. * current_step * self.decay)
