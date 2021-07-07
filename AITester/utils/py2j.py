'''
Created on Apr 30, 2020

@author: Hassan Sartaj
@version: 1.0
'''
from py4j.java_gateway import JavaGateway

class Py2JavaCommunicator():
    @staticmethod
    def getUAVModelInfo():
        gateway = JavaGateway()
        uav_beh_model = gateway.entry_point.getUAVBehModel()
        uav_dm_model = gateway.entry_point.getUAVDomainModel()
        
        initial_state = uav_beh_model.getInitialStateName()
        final_state = uav_beh_model.getFinalStateName()
        states = uav_beh_model.getUAVStateNames()
        st2nst_map = uav_beh_model.getUAVState2NextStatesMap()
        actions = uav_beh_model.getUAVActionNames()
        st2act_map = uav_beh_model.getUAVState2ActionsMap()
        st_act_nst = uav_beh_model.getUAVStateActionNextState()
        properties = uav_dm_model.getPropertiesWithDomain()
        return (initial_state, final_state, states, st2nst_map, actions, st2act_map, st_act_nst, properties)
    
    @staticmethod
    def evaluateConstraints(flight_state, flight_data):
        gateway = JavaGateway()
        model_evaluator = gateway.entry_point.getModelEvaluator()
        hmp = gateway.jvm.java.util.HashMap()
        for key, value in flight_data.items():
            hmp.put(key, value)
        failed = model_evaluator.evaluateModel(flight_state, hmp)
        return failed


#for testing
# fstate='Takeoff'
# flight_data = {'airspeed': 500, 'groundspeed': 90}
# failnum = Py2JavaCommunicator.evaluateConstraints(fstate, flight_data)
# print('failnum: ', failnum)
# initial_state, final_state, states, st2nst_map, actions, st2act_map, properties = Py2JavaCommunicator.getUAVModelInfo()
# print("Initial: ", initial_state, " - Final: ", final_state, "\n")
# print("Received States:\n", states)
# print("Received Next States Map:\n")
# for key, value in st2nst_map.items():
#     print(key, value, "\n")
# print("Received Actions:\n", actions)
# print("Received State Actions Map:\n")
# for key, value in st2act_map.items():
#     print(key, value, "\n")
# print("Received Properties:\n")
# for key, value in properties.items():
#     print(key, value[0], value[1])
