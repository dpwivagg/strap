#include "WPILib.h"

/**
 * This is a demo program showing the use of the RobotBase class.
 * The SimpleRobot class is the base of a robot application that will automatically call your
 * Autonomous and OperatorControl methods at the right time as controlled by the switches on
 * the driver station or the field controls.
 */ 
class RobotStrap : public SimpleRobot
{
	private:
	
	RobotDrive myRobot; // robot drive system
	Joystick stick; // only joystick

	public:
		
		/* Constructor */
		RobotStrap();
		~RobotStrap();
		
		/* Member functions */
		void Test( void );
		void Autonomous( void );
		void OperatorControl( void );

};

/* Constructor */
RobotStrap::RobotStrap() :
	// Initialize all the member objects in the same order
	// declared within the body.
	myRobot(1, 2),
	stick(1)
{
	myRobot.SetExpiration(0.1);
}

/**
 * Runs during test mode
 */
void RobotStrap::Test( void )
{

}

/**
 * Drive left & right motors for 2 seconds then stop
 */
void RobotStrap::Autonomous( void )
{
	myRobot.SetSafetyEnabled(false);
	myRobot.Drive(-0.5, 0.0); 	// drive forwards half speed
	Wait(2.0); 				//    for 2 seconds
	myRobot.Drive(0.0, 0.0); 	// stop robot
}

/**
 * Runs the motors with arcade steering. 
 */
void RobotStrap::OperatorControl( void )
{
	DriverStation* ds = DriverStation::GetInstance();
	myRobot.SetSafetyEnabled(true);

	while (IsOperatorControl())
	{
		/* Portion of code that gets status updates */
		float voltage = ds -> GetBatteryVoltage();
		
		/* Portion of code that controls arm */
		bool launchValue;
		bool rewindValue;
		launchValue = stick.GetRawButton(1);
		rewindValue = stick.GetRawButton(2);
		if(launchValue)
		{
			//make arm launch forwards
			
		}
		if(rewindValue)
		{
			//rewind arm
		}
		
		/* Portion of code that sets up driving */
		myRobot.ArcadeDrive(stick); // drive with arcade style (use right stick)
		Wait(0.005);				// wait for a motor update time
	}
}

START_ROBOT_CLASS(RobotStrap);

