'''
Created on Apr 29, 2020

@author: Hassan Sartaj
@version: 1.0

A goal-based environment in which the goal will be to return to ground after flying
'''

import torch
import torch.cuda
import numpy as np
import random
from utils import UAVModel
from uav.apcmds import arm, disarm, takeoff_simple, takeoff_complex, loiter, return_to_launch,\
    land, move_forward, move_backward, move_down, move_up, turn_left, turn_right,\
    hold_position, hold_altitude, stop_sitl, get_vehicle_state,\
    reset_sitl, reset_mode
from utils.py2j import Py2JavaCommunicator

state_len = 10
# (AFS, THRUST, ALTITUDE, AIRSPEED, GROUNDSPEED, ROLL, PITCH, YAW, BATTERY, DISTANCE) = list(range(state_len))
(AFS, ALTITUDE, AIRSPEED, GROUNDSPEED, ROLL, PITCH, YAW, HEADING, BATTERY, DISTANCE) = list(range(state_len))

class UAVCopterEnv():
    def __init__(self, name):
        self.name = name
        self.initial_state = UAVModel.getInitialState()
        self.final_state = UAVModel.getFinalState()
        self.state_space = state_len #UAVModel.getStates()
        self.action_space = UAVModel.getActions()
        self.observation_space = UAVModel.getObserations()
#         self.state = None
        self.state = np.array(np.zeros(state_len, dtype = float))
        self.fpfile = open('fpfile.txt', 'a+')
        self.fpfile.write("\n-----------------------New Evaluation-----------------------\n\n")
        self.fpfile.flush()
        self.is_loiter = False
        
    def reset(self):
#         self.state = UAVModel.getInitialState()
        self.state[AFS] = UAVModel.getInitialState()
        self.state[ALTITUDE] = 0
        self.state[AIRSPEED] = 0
        self.state[GROUNDSPEED] = 0
        self.state[ROLL] = 0
        self.state[PITCH] = 0
        self.state[YAW] = 0
        self.state[HEADING] = 0
        self.state[BATTERY] = 0
        self.state[DISTANCE] = 0
    
    def step(self, action):
#         assert self.action_space.contains(action), "%r (%s) invalid"%(action, type(action))
#         next_states = UAVModel.getNextStatesNums(self.state)
        possible_actions = UAVModel.getPossibleActions(self.state[AFS].item())
        action_num=0
        failed = {"count":"0","failed":"","passed":""}
        if type(action) is np.ndarray:
            action_num = action.item()
        else:
            action_num = action
        
        if action_num not in possible_actions:
            reward=-1.0
            done=False
            return torch.from_numpy(self.state), reward, done, {},failed
            return np.array(self.state), reward, done, {},failed

        #perform action on ardupilot using dronekit 
        isDisarmed=self.perform_action(UAVModel.getActionName(action_num))
        done = False
        reward=0.0
        if not isDisarmed:
            uav_state = get_vehicle_state()
            next_states = UAVModel.getNextStates4StateAction(self.state[AFS].item(), action_num)
         

            self.fpfile.write("\n=> State: "+UAVModel.getStateName(self.state[AFS].item())+" - Action: "+UAVModel.getActionName(action_num))
            self.fpfile.flush()
            index = random.randrange(len(next_states))
    #         self.state[AFS] = next_states[index]
            self.set_state(next_states[index], uav_state)
            if self.state[AFS].item() == self.final_state:
                done = True
                self.fpfile.write("\n\n--------------------Path End------------------\n\n")
                self.fpfile.flush()

            #For eval: calculate average reward obtained per time step.
            #TODO: need to change the following lines
            # reward=0.0
            if not done:
                reward,failed = self.calculate_reward() #TODO: need to calculate discounted rewards
        else:
            done = True
            print("Vehicle disarmed!!")

#         return np.array(self.state), reward, done, {}
        return torch.from_numpy(self.state), reward, done, {},failed
    
    def perform_action(self, action):
        isDisarmed=False
        if action == "Arm":
            arm()
        elif action == "Disarm":
            disarm()
        elif action == "Takeoff":
            # isDisarmed=takeoff_simple()
            isDisarmed = takeoff_complex()
        elif action == "MoveForward":
            isDisarmed=move_forward()
        elif action == "MoveBackward":
            isDisarmed=move_backward()
        elif action == "IncreaseAltitude":
            isDisarmed=move_up()
        elif action == "DecreaseAltitude":
            isDisarmed=move_down()
        elif action == "TurnLeft":
            isDisarmed=turn_left()
        elif action == "TurnRight":
            isDisarmed=turn_right()
        elif action == "HoldPosition":
            isDisarmed=hold_position()
        elif action == "HoldAltitude":
            isDisarmed=hold_altitude()
        elif action == "StartTaxi":
            pass
        elif action == "EndTaxi":
            pass
        elif action == "Loiter": 
            isDisarmed=loiter()
            self.is_loiter = True
        elif action == "ReturnToLaunch":
            return_to_launch()
        elif action == "Land":
            land()
            
        if self.is_loiter:
            isDisarmed=reset_mode()
            self.is_loiter = False
        return isDisarmed
    
    def calculate_reward(self):
        flight_state=UAVModel.getStateName(self.state[AFS])

        #TODO: need to change the following property names
        flight_data = {'altitude': self.state[ALTITUDE], 'airspeed': self.state[AIRSPEED], 'groundspeed': self.state[GROUNDSPEED],
                       'roll': self.state[ROLL], 'pitch': self.state[PITCH], 'yaw': self.state[YAW], 'heading': self.state[HEADING]}
        return Py2JavaCommunicator.evaluateConstraints(flight_state, flight_data)
    
    def set_state(self, afs_new, uav_state):
        self.state[AFS] = afs_new
        # self.state[ALTITUDE] = uav_state[0]
        # self.state[AIRSPEED] = uav_state[1]
        # self.state[GROUNDSPEED] = uav_state[2]
        # self.state[ROLL] = uav_state[3]
        # self.state[PITCH] = uav_state[4]
        # self.state[YAW] = uav_state[5]
        # self.state[HEADING] = uav_state[6]
        # self.state[BATTERY] = uav_state[7]
        # self.state[DISTANCE] = uav_state[8]

        #check None values
        for i in range(len(uav_state)):
            if uav_state[i] == None:
                uav_state[i] = 0

        #rounded values
        self.state[ALTITUDE] = round(uav_state[0],4)
        self.state[AIRSPEED] = round(uav_state[1],4)
        self.state[GROUNDSPEED] = round(uav_state[2],4)
        self.state[ROLL] = round(uav_state[3],4)
        self.state[PITCH] = round(uav_state[4],4)
        self.state[YAW] = round(uav_state[5],4)
        self.state[HEADING] = round(uav_state[6],4)
        self.state[BATTERY] = round(uav_state[7],4)
        self.state[DISTANCE] = round(uav_state[8],4)
        
    def close(self):
        stop_sitl()
    
    def reset_sitl(self):
        reset_sitl()

class UAVCopterEnvManager():
    def __init__(self, device):
        self.device = device
        self.env = UAVCopterEnv('Copter-Env')
        self.env.reset()
        self.done=False
        
    def reset(self):
        self.env.reset()
        
    def close(self):
        self.env.close()
        self.env.fpfile.close()
        
    def reset_uav(self):
        self.env.reset_sitl()
    
    def get_available_actions(self, state):
        return UAVModel.getPossibleActions(state)
        
    def get_total_actions(self):
        return len(self.env.action_space)
        
    def get_total_states(self):
        return state_len #len(self.env.state_space)
        
    def take_action(self, action):
        _, reward, self.done, _,failed = self.env.step(action)
        return torch.tensor([reward], device=self.device),failed
    
    def get_state(self):
        return torch.from_numpy(self.env.state)
    
    def get_state_space(self):
        return self.env.state_space
    
    def get_action_space(self):
        return self.env.action_space    