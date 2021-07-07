'''
Created on August 10, 2020

@author: Hassan Sartaj
@version: 1.0
'''
import time
import math
import subprocess
from subprocess import Popen
from dronekit import connect, VehicleMode, LocationGlobalRelative
from pymavlink import mavutil

vehicle = None

#TODO: change hard coded paths
#--Need to execute this once
#execute ardupilot sitl using cygwin
p = Popen("C:/cygwin/Cygwin.bat", stdin=subprocess.PIPE, stdout=subprocess.PIPE)
p.stdin.write('{}\n'.format('cd ardupilot/Tools/autotest').encode('utf-8'))
p.stdin.write('{}\n'.format('python2 sim_vehicle.py -v ArduCopter --console --map').encode('utf-8'))
p.stdin.close()
# child_id = p.pid
time.sleep(30) #time issue - varies from pc to pc
subprocess.Popen.kill(p)
# time.sleep(5)

print("Connecting to vehicle ...")
vehicle = connect("127.0.0.1:14550", wait_ready=True)

gnd_speed=5


#######################################################################################################################
#   Flight operations
#######################################################################################################################

# --> Ok
def arm():
    # Don't try to arm until autopilot is ready
    print (" Waiting for vehicle to initialise...")
    while not vehicle.is_armable:
        time.sleep(0.2)
    print (" Arming motors")
    # Copter should arm in GUIDED mode
    vehicle.mode    = VehicleMode("GUIDED")
    vehicle.armed   = True
    # Confirm vehicle armed before attempting to take off
    print(" Waiting for vehicle to arm...")
    while not vehicle.armed:
        # vehicle.mode    = VehicleMode("GUIDED")
        vehicle.armed   = True
        time.sleep(0.2)
    print(" Armed")

# --> Ok
def disarm():
    if vehicle.location.global_relative_frame.alt >= 2:
        land()
        time.sleep(0.2)
    vehicle.armed = False
    print(" Waiting for disarming...")
    while vehicle.armed:
        vehicle.armed = False
        time.sleep(1)
    print(" Disarmed")


# --> Ok
# v1 - simple takeoff
def takeoff_simple(altitude=20):
    print(" Taking off!")
    vehicle.simple_takeoff(altitude)  # Take off to target altitude

    while True:
        if not vehicle.armed:
           return True

        if vehicle.location.global_relative_frame.alt >= altitude * 0.95:
            print(" Reached target altitude")
            break
        time.sleep(0.2)
    return False

# --> Ok
def takeoff_complex(altitude=20):
    DEFAULT_TAKEOFF_THRUST = 0.7
    SMOOTH_TAKEOFF_THRUST = 0.6
    thrust = DEFAULT_TAKEOFF_THRUST
    while True:
        current_altitude = vehicle.location.global_relative_frame.alt
        if current_altitude >= altitude * 0.95:  # Trigger just below target alt.
            print("Reached target altitude")
            break
        elif current_altitude >= altitude * 0.6:
            thrust = SMOOTH_TAKEOFF_THRUST
        set_attitude(thrust=thrust)
        time.sleep(0.2)
        if not vehicle.armed:
           return True
    return False

# --> Ok
def reset_mode():
    vehicle.mode = VehicleMode("GUIDED")
    print(" Waiting for GUIDED mode...")
    while vehicle.mode.name is not "GUIDED":
        time.sleep(0.2)
    print(" GUIDED is changed.")

# --> Ok
def loiter():
    vehicle.mode = VehicleMode("LOITER")
    print(" Waiting for Loiter to start...")
    while vehicle.mode.name is not "LOITER":
        time.sleep(0.2)
        if not vehicle.armed:
           return True
    print(" Loiter is started.")
    return False

# may not be required
def start_taxi():
    pass

# may not be required
def end_taxi():
    pass

# TODO: 2  - single event
def increase_thrust():
    # set_attitude(0.7)
    pass

# TODO: 3 - single event
def decrease_thrust():
    pass

# --> Ok
def increase_altitude():
    print(" Increasing altitude...")
    set_attitude(thrust=0.7, duration=3)
    time.sleep(1)

# --> Ok
# Up & Down are similar to the move forward and move backward
def move_up():
    set_velocity_body(vehicle, gnd_speed, 0, 0)
    return True if not vehicle.armed else False

# --> Ok
def move_down():
    set_velocity_body(vehicle, -gnd_speed, 0, 0)
    return True if not vehicle.armed else False

# --> Ok
def decrease_altitude():
    print(" Decreasing altitude...")
    set_attitude(thrust=0.3, duration=3)

# may not be required - single event
def change_airspeed():
    speed_type = 0  # airspeed
    # speed = 10  # TODO: need to see +/- values
    speed = vehicle.airspeed+2  # TODO: need to see +/- values
    msg = vehicle.message_factory.command_long_encode(
        0, 0,  # target system, target component
        mavutil.mavlink.MAV_CMD_DO_CHANGE_SPEED,  # command
        0,  # confirmation
        speed_type,  # param 1
        speed,  # speed in meters/second
        -1, 0, 0, 0, 0  # param 3 - 7
    )
    vehicle.send_mavlink(msg)
    time.sleep(0.2)

# may not be required - single event
def increase_airspeed():
    print(" Increasing airspeed...")
    vehicle.airspeed = vehicle.airspeed+1

# may not be required - single event
def decrease_airspeed():
    print(" Decreasing airspeed...")
    vehicle.airspeed = vehicle.airspeed-1

# may not be required - single event
def increase_groundspeed():
    vehicle.groundspeed += 5

# may not be required - single event
def decrease_groundspeed():
    vehicle.groundspeed -= 5

# may not be required - single event
def change_groundspeed():
    speed_type = 1  # ground speed
    speed = 10  # TODO: need to see +/- values
    msg = vehicle.message_factory.command_long_encode(
        0, 0,  # target system, target component
        mavutil.mavlink.MAV_CMD_DO_CHANGE_SPEED,  # command
        0,  # confirmation
        speed_type,  # param 1
        speed,  # speed in meters/second
        -1, 0, 0, 0, 0  # param 3 - 7
    )
    vehicle.send_mavlink(msg)

# TODO: need to check if this is required?
def move_straight():
    move_forward()

# --> Ok
def move_forward():
    print("Moving forward...")
    set_attitude(roll_angle=vehicle.attitude.roll, pitch_angle=-5, yaw_angle=vehicle.attitude.yaw,
                 thrust=0.5, duration=3.21)

    return True if not vehicle.armed else False

# --> Ok
def move_backward():
    print("Moving backward...")
    set_attitude(roll_angle=vehicle.attitude.roll, pitch_angle=5, yaw_angle=vehicle.attitude.yaw,
                 thrust=0.5, duration=3)
    return True if not vehicle.armed else False

# --> Ok
def turn_right():
    print("Turning right...")
    # set_attitude(yaw_angle=5, yaw_rate=0.1, use_yaw_rate=True, thrust=0.5, duration=3)
    set_velocity_body(vehicle, 0, gnd_speed, 0)
    time.sleep(0.5)
    return True if not vehicle.armed else False

# --> Ok
def turn_left():
    print("Turning left...")
    # set_attitude(yaw_angle=-5, yaw_rate=0.1, use_yaw_rate=True, thrust=0.5, duration=3)
    set_velocity_body(vehicle, 0, -gnd_speed, 0)
    time.sleep(0.5)
    return True if not vehicle.armed else False

# --> Ok - need to change the lat/log/alt
def goto_location():
    point1 = LocationGlobalRelative(-35.361354, 149.165218, 20)
    vehicle.simple_goto(point1)
    point2 = LocationGlobalRelative(-35.363244, 149.168801, 20)
    # Going towards second point (with groundspeed 10 m/s)
    vehicle.simple_goto(point2, groundspeed=10)

# --> Ok
def hold_position():
    set_attitude(duration=3)
    time.sleep(1)
    return True if not vehicle.armed else False

# --> Ok
def hold_altitude():
    set_attitude(thrust=0.5, duration=3)
    time.sleep(1)
    return True if not vehicle.armed else False

# required for plane
def change_roll():
    pass

# required for plane
def change_pitch():
    pass

# required for plane
def change_yaw(heading=0, relative=False):
    if relative:
        is_relative = 1  # yaw relative to direction of travel
    else:
        is_relative = 0  # yaw is an absolute angle
    # create the CONDITION_YAW command using command_long_encode()
    msg = vehicle.message_factory.command_long_encode(
        0, 0,  # target system, target component
        mavutil.mavlink.MAV_CMD_CONDITION_YAW,  # command
        0,  # confirmation
        heading,  # param 1, yaw in degrees
        0,  # param 2, yaw speed deg/s
        1,  # param 3, direction -1 ccw, 1 cw
        is_relative,  # param 4, relative offset 1, absolute angle 0
        0, 0, 0)  # param 5 ~ 7 not used
    # send command to vehicle
    vehicle.send_mavlink(msg)

# may not be required
def change_velocity():
    pass

# required for plane
def drift():
    pass

# required for plane
def flip():
    pass

# TODO: 1- not supported by dronekit
def fly_circle():
    pass

# --> Ok
def return_to_launch():
    vehicle.mode = VehicleMode("RTL")
    print(" Waiting for RTL...")
    while vehicle.mode.name is not "RTL":
        time.sleep(0.2)
        if not vehicle.armed:
           return True
    print(" RTL started...")
    while vehicle.mode.name is "RTL":
        if vehicle.location.global_relative_frame.alt<1:
            break
        time.sleep(0.2)
    print(" RTL completed...")
    return False

# --> Ok
def land():
    vehicle.mode = VehicleMode("LAND")
    print(" Starting to land...")
    while vehicle.mode.name is not "LAND":
        time.sleep(0.2)
        if not vehicle.armed:
           return True


    print(" Landing is started.")
    while vehicle.mode.name is "LAND":
        time.sleep(0.2)
        if not vehicle.armed:
            return True
        if vehicle.location.global_relative_frame.alt<5: # if vehicle.location.global_relative_frame.alt<1:
            break
    print(" Landing is completed.")
    return False


# --> Ok - if use this, need to re-execute sitl
def reboot_autopilot():
    print("Rebooting AP.")
    msg = vehicle.message_factory.command_long_encode(
        0, 0,  # target_system, target_component
        mavutil.mavlink.MAV_CMD_PREFLIGHT_REBOOT_SHUTDOWN,  # cmd
        0,  # confirmation
        1,  # param 1, autopilot (reboot)
        0,  # param 2, onboard computer (do nothing)
        0,  # param 3, camera (do nothing)
        0,  # param 4, mount (do nothing)
        0, 0, 0)  # param 5 ~ 7 not used
    vehicle.send_mavlink(msg)
    time.sleep(2)

# --> Ok - if use this, need to re-execute sitl
def reboot_vehicle():
    vehicle.reboot()
    time.sleep(1)

# --> Ok
def close_vehicle():
    if vehicle is not None:
        print(" Closing vehicle...")
        vehicle.close()

        
def reset_sitl():
    close_vehicle()
    reboot_autopilot()
    print("Connecting to vehicle ...")
    global vehicle
    vehicle = connect("127.0.0.1:14550", wait_ready=True)
    
# --> check
def stop_sitl():
    print(" Stopping Ardupilot SITL...")
    close_vehicle()
#     pass

#######################################################################################################################
#   Helper functions
#######################################################################################################################

def send_attitude_target(roll_angle=0.0, pitch_angle=0.0,
                         yaw_angle=None, yaw_rate=0.0, use_yaw_rate=False,
                         thrust=0.5):
    """
    use_yaw_rate: the yaw can be controlled using yaw_angle OR yaw_rate.
                  When one is used, the other is ignored by Ardupilot.
    thrust: 0 <= thrust <= 1, as a fraction of maximum vertical thrust.
            Note that as of Copter 3.5, thrust = 0.5 triggers a special case in
            the code for maintaining current altitude.
    """
    if yaw_angle is None:
        # this value may be unused by the vehicle, depending on use_yaw_rate
        yaw_angle = vehicle.attitude.yaw
    # Thrust >  0.5: Ascend
    # Thrust == 0.5: Hold the altitude
    # Thrust <  0.5: Descend
    msg = vehicle.message_factory.set_attitude_target_encode(
        0,  # time_boot_ms
        1,  # Target system
        1,  # Target component
        0b00000000 if use_yaw_rate else 0b00000100,
        to_quaternion(roll_angle, pitch_angle, yaw_angle),  # Quaternion
        0,  # Body roll rate in radian
        0,  # Body pitch rate in radian
        math.radians(yaw_rate),  # Body yaw rate in radian/second
        thrust  # Thrust
    )
    vehicle.send_mavlink(msg)


def set_attitude(roll_angle=0.0, pitch_angle=0.0,
                 yaw_angle=None, yaw_rate=0.0, use_yaw_rate=False,
                 thrust=0.5, duration=0):
    """
    Note that from AC3.3 the message should be re-sent more often than every
    second, as an ATTITUDE_TARGET order has a timeout of 1s.
    In AC3.2.1 and earlier the specified attitude persists until it is canceled.
    The code below should work on either version.
    Sending the message multiple times is the recommended way.
    """
    send_attitude_target(roll_angle, pitch_angle,
                         yaw_angle, yaw_rate, use_yaw_rate, thrust)
    start = time.time()

    while time.time() - start < duration:
        send_attitude_target(roll_angle, pitch_angle,
                             yaw_angle, yaw_rate, use_yaw_rate, thrust)
        time.sleep(0.1)
    # Reset attitude, or it will persist for 1s more due to the timeout
    send_attitude_target(0, 0, 0, 0, True, thrust)


def to_quaternion(roll=0.0, pitch=0.0, yaw=0.0):
    """
    Convert degrees to quaternions
    """
    t0 = math.cos(math.radians(yaw * 0.5))
    t1 = math.sin(math.radians(yaw * 0.5))
    t2 = math.cos(math.radians(roll * 0.5))
    t3 = math.sin(math.radians(roll * 0.5))
    t4 = math.cos(math.radians(pitch * 0.5))
    t5 = math.sin(math.radians(pitch * 0.5))

    w = t0 * t2 * t4 + t1 * t3 * t5
    x = t0 * t3 * t4 - t1 * t2 * t5
    y = t0 * t2 * t5 + t1 * t3 * t4
    z = t1 * t2 * t4 - t0 * t3 * t5

    return [w, x, y, z]


# -- Define the function for sending mavlink velocity command in body frame
def set_velocity_body(vehicle, vx, vy, vz):
    """ Remember: vz is positive downward!!!
    http://ardupilot.org/dev/docs/copter-commands-in-guided-mode.html

    Bitmask to indicate which dimensions should be ignored by the vehicle
    (a value of 0b0000000000000000 or 0b0000001000000000 indicates that
    none of the setpoint dimensions should be ignored). Mapping:
    bit 1: x,  bit 2: y,  bit 3: z,
    bit 4: vx, bit 5: vy, bit 6: vz,
    bit 7: ax, bit 8: ay, bit 9:

    """
    msg = vehicle.message_factory.set_position_target_local_ned_encode(
        0,
        0, 0,
        mavutil.mavlink.MAV_FRAME_BODY_NED,
        0b0000111111000111,  # -- BITMASK -> Consider only the velocities
        0, 0, 0,  # -- POSITION
        vx, vy, vz,  # -- VELOCITY
        0, 0, 0,  # -- ACCELERATIONS
        0, 0)
    vehicle.send_mavlink(msg)
    vehicle.flush()

def print_vehicle_state(title="Info"):
    print("\n-------------"+title+"-------------")
    print(" Global Location: %s" % vehicle.location.global_frame)
    print(" Global Location (relative altitude): %s" % vehicle.location.global_relative_frame)
    # print(" Local Location: %s" % vehicle.location.local_frame)
    print(" Attitude: %s" % vehicle.attitude)
    print(" Velocity: %s" % vehicle.velocity)
    # print(" GPS: %s" % vehicle.gps_0)
    print(" Battery: %s" % vehicle.battery)
    print(" Last Heartbeat: %s" % vehicle.last_heartbeat)
    print(" Heading: %s" % vehicle.heading)
    print(" System status: %s" % vehicle.system_status.state)
    print(" Groundspeed: %s" % vehicle.groundspeed)  # settable
    print(" Airspeed: %s" % vehicle.airspeed)  # settable
    print(" Mode: %s" % vehicle.mode.name)  # settable
    # print(" Armed: %s" % vehicle.armed)  # settable
    print("---------------------------------------\n")

################################################################################################
#Define callback for `vehicle.attitude` observer
################################################################################################
last_attitude_cache = None
def attitude_callback(self, attr_name, value):
    # `attr_name` - the observed attribute (used if callback is used for multiple attributes)
    # `self` - the associated vehicle object (used if a callback is different for multiple vehicles)
    # `value` is the updated attribute value.
    global last_attitude_cache
    # Only publish when value changes
    if value!=last_attitude_cache:
        print(" CALLBACK: Attitude changed to", value)
        last_attitude_cache=value

def get_vehicle_state():
    #TODO: need to get change the thrust value
    return [vehicle.location.global_relative_frame.alt, vehicle.airspeed,
            vehicle.groundspeed, vehicle.attitude.roll, vehicle.attitude.pitch, 
            vehicle.attitude.yaw, vehicle.heading, vehicle.battery.voltage, vehicle.rangefinder.distance]
#     return (vehicle.thrust, vehicle.location.global_relative_frame.alt, vehicle.airspeed, 
#             vehicle.groundspeed, vehicle.attitude.RollAngle, vehicle.attitude.PitchAngle, 
#             vehicle.attitude.YawAngle, vehicle.battery, vehicle.rangefinder.distance)

# def get_vehicle_state():
#     # state = {}
#     # state[0] = vehicle.mode
#     # state[1] = vehicle.location.global_relative_frame.alt
#     # state[2] = vehicle.airspeed
#     # state[3] = vehicle.groundspeed
#     # state[4] = vehicle.heading
#     # state[5] = vehicle.attitude.pitch
#     # state[6] = vehicle.attitude.roll
#     # state[7] = vehicle.attitude.yaw
#     # state[8] = vehicle.battery.voltage
#     # state[9] = vehicle.velocity
# 
#     return (vehicle.mode.name, vehicle.location.global_relative_frame.alt, vehicle.airspeed,
#             vehicle.groundspeed, vehicle.heading, vehicle.attitude.pitch, vehicle.attitude.roll,
#             vehicle.attitude.yaw, vehicle.battery.voltage, vehicle.velocity)



#######################################################################################################################
#   Main - For testing purposes
#######################################################################################################################
if __name__=='__main__':
    # connect_to_uav()
    arm()
    takeoff_simple(20)
    # takeoff_complex(30)

    for _ in range(5):
        increase_altitude()
        state = get_vehicle_state()
        print("[STATE]:- Mode:%s, Alt:%s, AS:%s, GS:%s, Heading:%s, "
              "Pitch:%s, Roll:%s, Yaw:%s, Battery:%s, Velocity:%s" % (state))

    for _ in range(5):
        move_forward()
        time.sleep(1)
        state = get_vehicle_state()
        print("[STATE] ", state)
    # print_vehicle_state("Move forward")

    # for _ in range(5):
    #     increase_airspeed()
    # print_vehicle_state("Increase Airspeed")
    #
    # for _ in range(5):
    #     decrease_airspeed()
    # print_vehicle_state("Decrease Airspeed")

    # for _ in range(5):
    #     change_airspeed()
    # print_vehicle_state("Increase Airspeed")


    # loiter()
    # print_vehicle_state("Loiter")
    # time.sleep(20)
    # reset_mode()
    # print_vehicle_state("Mode Change")

    # for _ in range(5):
    #     print("Holding Position...")
    #     hold_position()
    # print_vehicle_state("Holding Position")
    #
    # for _ in range(5):
    #     print("Holding Altitude...")
    #     hold_altitude()
    # print_vehicle_state("Holding Altitude")
    #
    # for _ in range(5):
    #     turn_left()
    # print_vehicle_state("Turn Left")

    # for _ in range(5):
    #     turn_right()
    # print_vehicle_state("Turn Right")

    # for _ in range(5):
    #     goto_location()
    #     time.sleep(1)
    # print_vehicle_state("Go To")

    for _ in range(5):
        move_backward()
        time.sleep(1)
    print_vehicle_state("Move backward")


    # for _ in range(5):
    #     move_up()
    # print_vehicle_state("MoveUp")
    #
    # for _ in range(5):
    #     move_down()
    # print_vehicle_state("MoveDown")


    # land()
    # print_vehicle_state("Land")

    return_to_launch()

    disarm()
    print_vehicle_state("Disarm")

    # print("Rebooting autopilot")
    # reboot_autopilot()

    # print("Rebooting uav")
    # reboot_vehicle()

    stop_sitl()