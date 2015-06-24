using System.Globalization;
using Newtonsoft.Json.Linq;

namespace Thermostatic.Thermostat
{
    public class ApiInterface
    {
        private readonly WebInterface _webInterface;

        public ApiInterface(Thermostat thermostat)
        {
            _webInterface = new WebInterface(thermostat.Id);
            UseOrCreatThermostat();
        }

        private void UseOrCreatThermostat()
        {
            _webInterface.PutThermostatData("", "", "");
        }

        public JObject GetThermostatData()
        {
            return JObject.Parse(_webInterface.GetThermostatData());
        }

        public string GetThermostatData(string key)
        { 
            return GetThermostatData().Property("thermostat").Value.SelectToken(key).Value<string>();
        }

        public string GetRawThermostatData(string key)
        {
            return GetThermostatData().Property("thermostat").Value.SelectToken(key).ToString();
        }

        public void UpdateThermostat(string route, string elementName, string value)
        {
            _webInterface.PutThermostatData(route, elementName, value);
        }

        public void UpdateThermostat(string route, string elementName, float value)
        {
            UpdateThermostat(route, elementName, value.ToString(CultureInfo.InvariantCulture));
        }

        public void UpdateThermostat(string route, string elementName, bool value)
        {
            UpdateThermostat(route, elementName, value ? "off" : "on");
        }

        public void UpdateThermostat(string route, string elementName, WeekProgram value)
        {
            _webInterface.PutThermostatData(route, elementName, value);
        }
    }
}