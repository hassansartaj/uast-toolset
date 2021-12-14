'''
Created on Apr 30, 2020

@author: Hassan Sartaj
@version: 1.0
'''

from utils.py2j import Py2JavaCommunicator


#UAV flight actions from the behavioral profile
NUM_UAV_PRO_ACTIONS=29
(ARM, DISARM, TAKEOFF, START_TAXI, END_TAXI, LOITER, INCREASE_ALTITUDE, INCREASE_AIRSPEED, INCREASE_GROUNDSPEED, 
DECREASE_ALTITUDE, DECREASE_AIRSPEED, DECREASE_GROUNDSPEED, MOVE_FORWARD, MOVE_BACKWARD, TURN_LEFT, TURN_RIGHT, INCREASE_THRUST, 
DECREASE_THRUST, GOTO_LOCATION, HOLD_POSITION, HOLD_ALTITUDE, CHANGE_ROLL, CHANGE_VELOCITY, CHANGE_PITCH, CHANGE_YAW, 
FLIP, FLY_CIRCLE, RETURN_TO_LAUNCH, LAND) = list(range(NUM_UAV_PRO_ACTIONS))

PRO_ACTIONS_MAPPING={ARM: "Arm", DISARM: "Disarm", TAKEOFF: "Takeoff", START_TAXI: "StartTaxi", END_TAXI: "EndTaxi", LOITER: "Loiter", 
                 INCREASE_ALTITUDE: "IncreaseAltitude", INCREASE_AIRSPEED: "IncreaseAirspeed", INCREASE_GROUNDSPEED: "IncreaseGroundspeed", 
                 DECREASE_ALTITUDE: "DecreaseAltitude", DECREASE_AIRSPEED: "DecreaseAirspeed", DECREASE_GROUNDSPEED: "DecreaseGroundspeed", 
                 MOVE_FORWARD: "MoveForward", MOVE_BACKWARD: "MoveBackward", TURN_LEFT: "TurnLeft", TURN_RIGHT: "TurnRight", INCREASE_THRUST: "IncreaseThrust", 
                 DECREASE_THRUST: "DecreaseThurst", GOTO_LOCATION: "GotoLocation", HOLD_POSITION: "HoldPosition", HOLD_ALTITUDE: "HoldAltitude", 
                 CHANGE_ROLL: "ChangeRoll", CHANGE_VELOCITY: "ChangeVelocity", CHANGE_PITCH: "ChangePitch", CHANGE_YAW: "ChangeYaw", 
                 FLIP: "Flip", FLY_CIRCLE: "FlyCircle", RETURN_TO_LAUNCH: "ReturnToLaunch", LAND: "Land"}

INV_PRO_ACTIONS_MAPPING = dict(map(reversed, PRO_ACTIONS_MAPPING.items()))


#UAV flight states from the behavioral profile
NUM_UAV_PRO_STATES=17
(ARMED, DISARMED, TAKEOFF, CLIMB, CRUISE, DESCENT, LOITER, CIRCLE, ALTITUDE_HOLD, POSITION_HOLD, 
DRIFTING, FLIPING, TURNING_RIGHT, TURNING_LEFT, FLYING_STRAIGHT, APPROACH, LANDING) = list(range(NUM_UAV_PRO_STATES))

PRO_STATES_MAPPING={ARMED: "Armed", DISARMED: "Disarmed", TAKEOFF: "TakeOff", CLIMB: "Climb", CRUISE: "Cruise", DESCENT: "Descent", 
                    LOITER: "Loiter", CIRCLE: "Circle", ALTITUDE_HOLD: "AltitudeHold", POSITION_HOLD: "PositionHold", DRIFTING: "Drifting", 
                    FLIPING: "Fliping", TURNING_RIGHT: "TurningRight", TURNING_LEFT: "TurningLeft", FLYING_STRAIGHT: "FlyingStraight", 
                    APPROACH: "Approach", LANDING: "Landing"}

INV_PRO_STATES_MAPPING = dict(map(reversed, PRO_STATES_MAPPING.items()))


#UAV flight data from the structural profile
NUM_UAV_PRO_OBS=43

#get modeled states, actions, and properties (observations)
initial_state, final_state, states, st2nst_map, actions, st2act_map, st_act_nst, properties = Py2JavaCommunicator.getUAVModelInfo()
# print("Received: ", states, actions, properties)

NUM_UAV_ACTIONS=len(actions)
NUM_UAV_STATES=len(states)
NUM_UAV_OBS=len(properties)

#Extract modeled actions
#name->number
ACTIONS_MAPPING = {key: INV_PRO_ACTIONS_MAPPING[key] for key in actions}
counter=0
for key in ACTIONS_MAPPING.keys():
    ACTIONS_MAPPING[key]=counter
    counter+=1
#number->name
INV_ACTIONS_MAPPING = dict(map(reversed, ACTIONS_MAPPING.items()))

#Extract modeled states
# STATES_MAPPING = {key: INV_PRO_STATES_MAPPING[key] for key in states}
#name->number
STATES_MAPPING = {}
st_counter=NUM_UAV_STATES
for st in states:
    new_item = None
    if "_" in st:
        st_counter+=1
        new_item = {st:st_counter}
    else:
        new_item = {st:INV_PRO_STATES_MAPPING[st]}
    STATES_MAPPING.update(new_item)

counter=0
for key in STATES_MAPPING.keys():
    STATES_MAPPING[key]=counter
    counter+=1         
#number->name
INV_STATES_MAPPING = dict(map(reversed, STATES_MAPPING.items()))


#new state example
# state_len = 10
# state_space = [0 for i in range(state_len)]
# (AFS, THRUST, ALTITUDE, AIRSPEED, GROUNDSPEED, ROLL, PITCH, YAW, BATTERY, DISTANCE) = list(range(state_len))
# state_space[AFS]=3
# state_space[ROLL]=-1.0
# state_space[DISTANCE]=5

# print("NUM: A={}, S={}, O={}".format(NUM_UAV_ACTIONS, NUM_UAV_STATES, NUM_UAV_OBS))
# print("\nACTIONS_MAPPING: \n", ACTIONS_MAPPING)
# print("\nINV_ACTIONS_MAPPING: \n", INV_ACTIONS_MAPPING)
# print("\nSTATES_MAPPING: \n", STATES_MAPPING)
# print("\nINV_STATES_MAPPING: \n", INV_STATES_MAPPING)
# print("\nObservations: \n")
# for key, value in properties.items():
#     print(key, value[0], value[1])

# print("-> st_act_nst: \n")
# for sans in st_act_nst:
#     print(sans.split("-"))
# matching = [s for s in st_act_nst if "Climb_FlyingStraight-TurnRight" in s]
# print("-> matching st_act_nst: ", matching)

class UAVModel():
    @staticmethod
    def getStatesCount():
        return NUM_UAV_STATES
    
    @staticmethod
    def getActionsCount():
        return NUM_UAV_ACTIONS
    
    @staticmethod
    def getObserationsCount():
        return NUM_UAV_OBS
    
    @staticmethod
    def getStates():
        return list(INV_STATES_MAPPING.keys())
    
    @staticmethod
    def getInitialStateName():
        return initial_state
    
    @staticmethod
    def getInitialState():
        return STATES_MAPPING[initial_state]
    
    @staticmethod
    def getFinalStateName():
        return final_state
    
    @staticmethod
    def getFinalState():
        return STATES_MAPPING[final_state]
    
    @staticmethod
    def isFinalState(number):
        return STATES_MAPPING[final_state]==number
    
    @staticmethod
    def getStatesNames():
        return STATES_MAPPING.keys()
    
    @staticmethod
    def getStateName(number):
        assert type(number) is int or float, "%r (%s) invalid state number "%(number, type(number))
        return INV_STATES_MAPPING[int(number)]
    
    @staticmethod
    def getStateNumber(name):
        assert type(name) is str, "%r (%s) invalid state name "%(name, type(name))
        return STATES_MAPPING[name]
    
    @staticmethod
    def getNextStates(name):
        for key, next_states in st2nst_map.items():
            if key==name:
                return next_states
    
    @staticmethod
    def getNextStatesNums(number):
        next_state_nums = []
        next_states = st2nst_map.get(UAVModel.getStateName(number))
        for ns in next_states:
            if type(ns) is str:
                next_state_nums.append(UAVModel.getStateNumber(ns))
        return next_state_nums 
               
    @staticmethod
    def hasNextState(st_num, nst_num):
        if UAVModel.getStateName(nst_num) in st2nst_map.get(UAVModel.getStateName(st_num)):
            return True
        else:
            return False
    
    @staticmethod
    def getActions():
        return list(INV_ACTIONS_MAPPING.keys())
    
    @staticmethod
    def getActionsNames():
        return ACTIONS_MAPPING.keys()
    
    @staticmethod
    def getActionName(number):
        assert type(number) is int, "%r (%s) invalid action number "%(number, type(number))
        try:
            a = INV_ACTIONS_MAPPING[number]
        except KeyError:
            a = "UKey"
        return a
    
    @staticmethod
    def getActionNumber(name):
        assert type(name) is str, "%r (%s) invalid action name "%(name, type(name))
        return ACTIONS_MAPPING[name]
    
    @staticmethod
    def getPossibleActions(state):
        possible_actions = []
        for key, values in st2act_map.items():
            if key==UAVModel.getStateName(state):
                for act in values:
                    possible_actions.append(UAVModel.getActionNumber(act))
                    
        return possible_actions
    
    @staticmethod
    def getNextStates4StateAction(state_num, action_num):
        st_name = UAVModel.getStateName(state_num)
        act_name = UAVModel.getActionName(action_num)
        matching = [s for s in st_act_nst if st_name+"-"+act_name in s]
        next_states=[]
        for m in matching:
            sm = m.split("-")
            next_states.append(UAVModel.getStateNumber(sm[len(sm)-1]))
        return next_states

    @staticmethod
    def getObserations():
        return properties
    

