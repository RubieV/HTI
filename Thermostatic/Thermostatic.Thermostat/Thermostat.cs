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
using Thermostatic.Thermostat.Properties;

namespace Thermostatic.Thermostat
{
    public class Thermostat
    {
        private static int _id = Settings.Default.ThermostatID;

        public class ThermostatWebInterfaceFactory
        {
            public static HttpClient Get()
            {
                return new HttpClient {BaseAddress = new Uri(Settings.Default.ThermostatWebInterface)};
            }
        }

        public static int UseOrCreateThermostat(int thermostatId)
        {
            var response = ThermostatWebInterfaceFactory.Get().PutAsync(Settings.Default.Path + _id, null).Result;

            if (response.StatusCode != HttpStatusCode.OK && response.StatusCode != HttpStatusCode.Created)
                throw new Exception("Allready existing id: " + _id);
            
            UseThermostat(thermostatId);
            return thermostatId;
        }

        public static void UseThermostat(int thermostatId)
        {
            _id = thermostatId;
            Settings.Default.ThermostatID = thermostatId;
        }

        private static XmlDocument GetRawThermostatData()
        {
            var returningXml = new XmlDocument();

            returningXml.LoadXml(
                ThermostatWebInterfaceFactory.Get()
                    .GetAsync(Settings.Default.Path + _id)
                    .Result.Content.ReadAsStringAsync()
                    .Result);

            return returningXml;
        }

        private static XmlDocument UpdateLocalThermostatData()
        {
            var thermostatData = GetRawThermostatData();
            var serializer = new StringWriter();
            thermostatData.Save(serializer);
            
            Settings.Default.ThermostatData = serializer.ToString();
            Settings.Default.LastUpdate = DateTime.Now;
            Settings.Default.Save();

            return thermostatData;
        }

        private static XmlDocument GetThermostatData()
        {
            if (DateTime.Now.Subtract(Settings.Default.LastUpdate).TotalSeconds >= 30) 
                return UpdateLocalThermostatData();
            
            var thermostatDocument = new XmlDocument();
            thermostatDocument.LoadXml(Settings.Default.ThermostatData);
            return thermostatDocument;
        }

        public static string GetDay()
        {
            return GetThermostatData().GetElementsByTagName("current_day").Item(0).InnerText;
        }

        public static string GetTime()
        {
            return GetThermostatData().GetElementsByTagName("time").Item(0).InnerText;
        }

        public static float GetTargetTemprature()
        {
            return float.Parse(GetThermostatData().GetElementsByTagName("target_temperature").Item(0).InnerText, CultureInfo.InvariantCulture);
        }

        public static float GetCurrentTemprature()
        {
            return float.Parse(GetThermostatData().GetElementsByTagName("current_temperature").Item(0).InnerText, CultureInfo.InvariantCulture);
        }

        public static float GetDayTemprature()
        {
            return float.Parse(GetThermostatData().GetElementsByTagName("day_temperature").Item(0).InnerText, CultureInfo.InvariantCulture);
        }

        public static float GetNightTemprature()
        {
            return float.Parse(GetThermostatData().GetElementsByTagName("night_temperature").Item(0).InnerText, CultureInfo.InvariantCulture);
        }

        public static bool IsLocked()
        {
            return GetThermostatData().GetElementsByTagName("week_program_state").Item(0).InnerText != "on";
        }


    }
}
