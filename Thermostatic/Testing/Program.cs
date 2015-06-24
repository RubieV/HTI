using System;
using Thermostatic.Thermostat;
using Thermostatic.Thermostat.Properties;

namespace Testing
{
    class Program
    {
        static void Main(string[] args)
        {
            var thermostat = new Thermostat(1337);
            
            var testweek = new WeekProgram
            {
                State = thermostat.WeekprogrammIsEnabled ? "on" : "off", 
                Days = new Days{ 
                    Monday = new Thermostatic.Thermostat.Program(), 
                    Tuesday = new Thermostatic.Thermostat.Program(),
                    Wednesday = new Thermostatic.Thermostat.Program(),
                    Thursday = new Thermostatic.Thermostat.Program(),
                    Friday = new Thermostatic.Thermostat.Program(),
                    Saturday = new Thermostatic.Thermostat.Program(),
                    Sunday = new Thermostatic.Thermostat.Program()
                } 
            };
 
            for (int n = 0; n < 10; n++)
            {
                //5 Day, then 5 Night
                testweek.Days.Monday.Switches.Add(new Switch(n < 5, true, "13:37"));
                testweek.Days.Tuesday.Switches.Add(new Switch(n < 5, true, "13:37"));
                testweek.Days.Wednesday.Switches.Add(new Switch(n < 5, true, "13:37"));
                testweek.Days.Thursday.Switches.Add(new Switch(n < 5, true, "13:37"));
                testweek.Days.Friday.Switches.Add(new Switch(n < 5, true, "13:37"));
                testweek.Days.Saturday.Switches.Add(new Switch(n < 5, true, "13:37"));
                testweek.Days.Sunday.Switches.Add(new Switch(n < 5, true, "13:37"));
            }

            thermostat.WeekProgram = testweek;
 
            Console.WriteLine("written");
            var test = thermostat.WeekProgram;
            
            Console.WriteLine(thermostat.CurrentTemperature);
            Console.WriteLine(thermostat.TargetTemperature);
            thermostat.TargetTemperature = 6f;
            Console.WriteLine(thermostat.TargetTemperature);
            
            Console.ReadKey();
        }
    }
}
