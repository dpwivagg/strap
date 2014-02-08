// Pack this nonsense
package edu.wpi.first.wpilibj.templates;

// Import the nonsense
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStationLCD.Line;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Victor;


public class RobotStrap extends SimpleRobot
{
    /* Set up all the objects that we will be using */
    private final RobotDrive drivetrain;
    private final Joystick leftStick;
    private final Joystick rightStick;
    private final Jaguar arm1;
    private final Jaguar arm2;
    private final Jaguar conv1;
    private final Victor conv2;
    private final Compressor c;
    private final DigitalInput armfwdlim;
    private final DigitalInput armbwdlim;
    private final Solenoid latch;
    private final Solenoid s2;
    private final Solenoid s3;
    
    /* Set up all the constant values */
    // Drive system motor ports
    private final int FRONT_LEFT_MOTOR_PORT = 1;
    private final int FRONT_RIGHT_MOTOR_PORT = 2;
    private final int REAR_LEFT_MOTOR_PORT = 3;
    private final int REAR_RIGHT_MOTOR_PORT = 4;
    
    // Driver joystick ports
    private final int LEFT_JOYSTICK_PORT = 1;
    private final int RIGHT_JOYSTICK_PORT = 2;
    
    // Arm motor ports
    private final int ARM1_MOTOR_PORT = 7;
    private final int ARM2_MOTOR_PORT = 8;
    
    // Conveyor motor ports
    private final int CONV1_MOTOR_PORT = 5;
    private final int CONV2_MOTOR_PORT = 6;
    
    // Pneumatic system ports
    private final int SPIKE_RELAY_PORT = 1;
    private final int PRESSURE_SWITCH_PORT = 1;
    
    // Limit switch ports
    private final int ARM_FWD_LIM_PORT = 3;
    private final int ARM_BWD_LIM_PORT = 2;
    
    /* Constructor */
    public RobotStrap()
    {
        // Throw caution to the wind
        getWatchdog().setEnabled(false);
        
        drivetrain = new RobotDrive(FRONT_LEFT_MOTOR_PORT, REAR_LEFT_MOTOR_PORT, FRONT_RIGHT_MOTOR_PORT, REAR_RIGHT_MOTOR_PORT);
        leftStick = new Joystick(LEFT_JOYSTICK_PORT);
        rightStick = new Joystick(RIGHT_JOYSTICK_PORT);
        arm1 = new Jaguar(ARM1_MOTOR_PORT);
        arm2 = new Jaguar(ARM2_MOTOR_PORT);
    	c = new Compressor(PRESSURE_SWITCH_PORT, SPIKE_RELAY_PORT);
    	conv1 = new Jaguar(CONV1_MOTOR_PORT);
        conv2 = new Victor(CONV2_MOTOR_PORT);
        armfwdlim = new DigitalInput(ARM_FWD_LIM_PORT);
    	armbwdlim = new DigitalInput(ARM_BWD_LIM_PORT);
        latch = new Solenoid(1);
        s2 = new Solenoid(2);
        s3 = new Solenoid(3);
    }
    
    /* Autonomous */
    public void autonomous() {
        drivetrain.drive(0.5, 0.0); // drive 50% forward with 0% turn
        Timer.delay(2.0); // wait 2 seconds
        drivetrain.drive(0.0, 0.0); // drive 0% forward, 0% turn (stop)
    }
    
    /* Telo-op */
    public void operatorControl()
    {
    	// Start the auto compressor manager and hold all solenoids
    	latch.set(latch.get());
        s2.set(s2.get());
        s3.set(s3.get());
        c.start();
        
        while (true && isOperatorControl() && isEnabled())
        {
            // Print status if the button is pressed
            if(rightStick.getRawButton(11))
    		printStatus();
            
            // enable soleniod swapping
            if(leftStick.getRawButton(7))
                latch.set(!latch.get());
            if(leftStick.getRawButton(6))
                s2.set(!s2.get());
            if(leftStick.getRawButton(8))
                s3.set(!s3.get());
            
            // get conveyor stuff going
            boolean upValue = leftStick.getRawButton(3);
            boolean downValue = leftStick.getRawButton(2);
            double speedConv = (leftStick.getAxis(Joystick.AxisType.kZ) + (-1)) / 2 * -1;
            if(upValue)
            {
                conv1.set(2 * speedConv);
                conv2.set(1 * speedConv);
            }
            else if(downValue)
            {
                conv1.set(-2 * speedConv);
                conv2.set(-1 * speedConv);
            }
            else
            {
                conv1.set(0);
                conv2.set(0);
            } 
            
            // limit switch auto latching
            if(armbwdlim.get())
            {
                latch.set(true);
                printStuff("Latch fired by limit switch", Line.kUser3);
            }
            
    	    // Determine the speed to throw the arm at
            if(leftStick.getAxis(Joystick.AxisType.kY) >= 0 && !armfwdlim.get())
            {
                arm1.set(leftStick.getAxis(Joystick.AxisType.kY));
                arm2.set(leftStick.getAxis(Joystick.AxisType.kY));
            }
            if(leftStick.getAxis(Joystick.AxisType.kY) <= 0 && !armbwdlim.get())
            {
                arm1.set(leftStick.getAxis(Joystick.AxisType.kY));
                arm2.set(leftStick.getAxis(Joystick.AxisType.kY));
            }
            
            // arcade drive
            drivetrain.arcadeDrive(rightStick);
            
            // delay
            Timer.delay(0.005);
        }
    }
    
    /* Print robot status via driver station API */
    public void printStatus()
    {
        // Get an instance of the driver station to use its API
    	DriverStation ds = DriverStation.getInstance();
         
        // Get driver station LCD
        DriverStationLCD dslcd = DriverStationLCD.getInstance();
        
    	// Get important variable data
    	double voltage = ds.getBatteryVoltage();
    
        // Print the voltage
        dslcd.println(DriverStationLCD.Line.kUser6, 1, "Battery Voltage: " + String.valueOf(voltage));
        dslcd.updateLCD();
    }

    /* Print a message to the user message center */
    public void printStuff(String msg, Line type)
    {
        DriverStationLCD dslcd = DriverStationLCD.getInstance();
        dslcd.println(type, 1, msg);
        dslcd.updateLCD();
    }
    
    /* Test function */
    public void test()
    {
        
    }
}
