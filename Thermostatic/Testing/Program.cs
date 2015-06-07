using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Thermostatic.Thermostat;

namespace Testing
{
    class Program
    {
        static void Main(string[] args)
        {

            Console.WriteLine(Thermostat.GetCurrentTemprature());
            

            Console.ReadKey();
        }
    }
}
