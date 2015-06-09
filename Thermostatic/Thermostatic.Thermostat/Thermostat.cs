using System;
using System.Globalization;
using System.IO;
using System.Net.Http;
using System.Xml;
using Newtonsoft.Json.Linq;
using Thermostatic.Thermostat.Properties;

namespace Thermostatic.Thermostat
{
    public class Thermostat
    {
        private int _id = Settings.Default.ThermostatID;
        private bool _cacheInvalid = true;

        public Thermostat() {}

        public Thermostat(int id)
        {
            Id = id;
        }

        public int Id
        {
            get { return _id; }
            set
            {
                _id = UseOrCreateThermostat(value);
            }
        }

        public string Day
        {
            get { return GetThermostatData("current_day"); }
            set { UpdateThermostat("day", "current_day", value); }
        }

        public string Time
        {
            get { return GetThermostatData("time"); }
            set { UpdateThermostat("time", "time", value); }
        }

        public float CurrentTemperature
        {
            get { return float.Parse(GetThermostatData("current_temperature"), CultureInfo.InvariantCulture); }
        }

        public float TargetTemperature
        {
            get { return float.Parse(GetThermostatData("target_temperature"), CultureInfo.InvariantCulture); }
            set { UpdateThermostat("currentTemperature", "current_temperature", value); }
        }

        public float DayTemperature
        {
            get
            {
                return float.Parse(GetThermostatData("day_temperature"), CultureInfo.InvariantCulture);
            }
            set
            {
                UpdateThermostat("dayTemperature", "day_temperature", value);
            }
        }

        public float NightTemperature
        {
            get
            {
                return float.Parse(GetThermostatData("night_temperature"),
                    CultureInfo.InvariantCulture);
            }
            set
            {
                UpdateThermostat("nightTemperature", "night_temperature", value);
            }
        }

        public bool LockedState
        {
            get { return GetThermostatData("week_program_state") != "on"; }
            set
            {
                UpdateThermostat("weekProgramState", "week_program_state", value);
            }
        }

        private static class ThermostatWebInterfaceFactory
        {
            public static HttpClient Get()
            {
                return new HttpClient {BaseAddress = new Uri(Settings.Default.ThermostatWebInterface) };
            }
        }

        public void UpdateThermostat(string route, string elementName, string value)
        {
            var jobject = new JObject();
            jobject[elementName] = value;
            
            ThermostatWebInterfaceFactory.Get().PutAsJsonAsync(Settings.Default.Path + Id + "/" + route, jobject).Wait();
            _cacheInvalid = true;
        }

        public void UpdateThermostat(string route, string elementName, float value)
        {
            UpdateThermostat(route, elementName, value.ToString(CultureInfo.InvariantCulture));
        }

        public void UpdateThermostat(string route, string elementName, bool value)
        {
            UpdateThermostat(route, elementName, value ? "off" : "on");
        }

        public int UseOrCreateThermostat(int thermostatId)
        {
            ThermostatWebInterfaceFactory.Get().PutAsync(Settings.Default.Path + Id, null).Wait();

            Settings.Default.ThermostatID = thermostatId;
            Settings.Default.LastUpdate = new DateTime(2000, 1, 1);
            Settings.Default.Save();

            _cacheInvalid = true;

            return thermostatId;
        }

        private XmlDocument GetThermostatData()
        {
            return CacheIsValid() ? 
                GetCachedThermostatData() : UpdateLocalThermostatData();
        }

        private string GetThermostatData(string key)
        {
            return GetThermostatData().GetElementsByTagName(key).Item(0).InnerText;
        }

        private bool CacheIsValid()
        {
            return !_cacheInvalid || DateTime.Now.Subtract(Settings.Default.LastUpdate)
                .TotalSeconds <= Settings.Default.CacheDurationSeconds;
        }

        private static XmlDocument GetCachedThermostatData()
        {
            var thermostatDocument = new XmlDocument();
            thermostatDocument.LoadXml(Settings.Default.ThermostatData);
            return thermostatDocument;
        }

        private XmlDocument UpdateLocalThermostatData()
        {
            var thermostatData = GetRawThermostatData();
            
            var serializer = new StringWriter();
            thermostatData.Save(serializer);

            Settings.Default.ThermostatData = serializer.ToString();
            Settings.Default.LastUpdate = DateTime.Now;
            Settings.Default.Save();

            _cacheInvalid = false;
            return thermostatData;
        }

        private XmlDocument GetRawThermostatData()
        {
            var returningXml = new XmlDocument();

            returningXml.LoadXml(
                ThermostatWebInterfaceFactory.Get()
                    .GetAsync(Settings.Default.Path + Id)
                    .Result.Content.ReadAsStringAsync()
                    .Result);

            return returningXml;
        }
    }
}
