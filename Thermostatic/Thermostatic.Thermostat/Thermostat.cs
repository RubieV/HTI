using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;
using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Serialization;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json.Schema;
using Thermostatic.Thermostat.Properties;

namespace Thermostatic.Thermostat
{
    public class Thermostat
    {
        private int _id = Settings.Default.ThermostatID;
        private bool _cacheInvalid;

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
                Settings.Default.ThermostatID = _id;
            }
        }

        public string Day
        {
            get { return GetThermostatData().GetElementsByTagName("current_day").Item(0).InnerText; }
            set { UpdateThermostat("day", "current_day", value); }
        }

        public string Time
        {
            get { return GetThermostatData().GetElementsByTagName("time").Item(0).InnerText; }
            set { UpdateThermostat("time", "time", value); }
        }

        public float CurrentTemperature
        {
            get
            {
                return float.Parse(GetThermostatData().GetElementsByTagName("current_temperature").Item(0).InnerText,
                    CultureInfo.InvariantCulture); 
            }
        }

        public float TargetTemperature
        {
            get
            {
                return float.Parse(GetThermostatData().GetElementsByTagName("target_temperature").Item(0).InnerText,
                    CultureInfo.InvariantCulture); 
            }
            set 
            {
                UpdateThermostat("currentTemperature", "current_temperature", value.ToString(CultureInfo.InvariantCulture));
            }
        }

        public float DayTemperature
        {
            get
            {
                return float.Parse(GetThermostatData().GetElementsByTagName("day_temperature").Item(0).InnerText,
                    CultureInfo.InvariantCulture);
            }
            set
            {
                UpdateThermostat("dayTemperature", "day_temperature", value.ToString(CultureInfo.InvariantCulture));
            }
        }

        public float NightTemperature
        {
            get
            {
                return float.Parse(GetThermostatData().GetElementsByTagName("night_temperature").Item(0).InnerText,
                    CultureInfo.InvariantCulture);
            }
            set
            {
                UpdateThermostat("nightTemperature", "night_temperature", value.ToString(CultureInfo.InvariantCulture));
            }
        }

        public bool LockedState
        {
            get { return GetThermostatData().GetElementsByTagName("week_program_state").Item(0).InnerText != "on"; }
            set
            {
                UpdateThermostat("weekProgramState", "week_program_state", value ? "off" : "on");
            }
        }

        public static class ThermostatWebInterfaceFactory
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

        public int UseOrCreateThermostat(int thermostatId)
        {
            ThermostatWebInterfaceFactory.Get().PutAsync(Settings.Default.Path + Id, null).Wait();
            
            Settings.Default.LastUpdate = new DateTime(2000, 1, 1);
            Settings.Default.Save();

            return thermostatId;
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

        private XmlDocument GetThermostatData()
        {
            if (_cacheInvalid || DateTime.Now.Subtract(Settings.Default.LastUpdate).TotalSeconds >= 30) 
                return UpdateLocalThermostatData();
            
            var thermostatDocument = new XmlDocument();
            thermostatDocument.LoadXml(Settings.Default.ThermostatData);
            return thermostatDocument;
        }
    }
}
