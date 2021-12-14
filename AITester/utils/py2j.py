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
        return eval(failed["count"]),failed
