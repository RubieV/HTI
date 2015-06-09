using System;
using Thermostatic.Thermostat;

namespace Testing
{
    class Program
    {
        static void Main(string[] args)
        {
            var thermostat = new Thermostat();

            Console.WriteLine(thermostat.Time);
            thermostat.Time = "12:00";
            Console.WriteLine("Setting: " + "12:00");
            Console.WriteLine(thermostat.Time);
            
            Console.ReadKey();
        }
    }
}
