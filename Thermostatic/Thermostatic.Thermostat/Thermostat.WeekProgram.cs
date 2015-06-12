using System.Collections.Generic;
using Newtonsoft.Json;

// ReSharper disable once CheckNamespace
namespace Thermostatic.Thermostat
{
    public class WeekProgram
    {
        [JsonProperty("state")]
        public string State;

        [JsonProperty("days")]
        public Days Days = new Days();
    }
    
    public class Days
    {
        public Program Monday = new Program();
        public Program Tuesday = new Program();
        public Program Wednesday = new Program();
        public Program Thursday = new Program();
        public Program Friday = new Program();
        public Program Saturday = new Program();
        public Program Sunday = new Program();
    }

    public class Program
    {
        [JsonProperty("switches")]
        public List<Switch> Switches = new List<Switch>();
    }

    public class Switch
    {
        [JsonProperty("type")]
        public string Type;

        [JsonProperty("state")]
        public string State;

        [JsonProperty("time")]
        public string Time;

        public Switch(bool isDay = true, bool isEnabled = false, string time = "00:00")
        {
            Type = isDay ? "day" : "night";
            State = isEnabled ? "on" : "off";
            Time = time;
        }
    }
}
