using System;
using System.Net;
using System.Net.Http;
using Newtonsoft.Json.Linq;
using Thermostatic.Thermostat.Properties;

namespace Thermostatic.Thermostat
{
    public class WebInterface
    {
        private bool _cacheInvalid;
        private readonly int _thermostatId;

        public WebInterface(int thermostatId)
        {
            _thermostatId = thermostatId;
            Settings.Default.LastUpdate = null;
            _cacheInvalid = true;
        }

        public HttpClient Get()
        {
            var client =  new HttpClient { BaseAddress = new Uri(Settings.Default.ThermostatWebInterface) };
            client.DefaultRequestHeaders.Add(HttpRequestHeader.Accept.ToString(), "application/json");
            return client;
        }

        private bool CacheIsValid()
        {
            return !_cacheInvalid && 
                Settings.Default.LastUpdate != null &&
                DateTime.Now.Subtract(Settings.Default.LastUpdate.GetValueOrDefault())
                    .TotalSeconds <= Settings.Default.CacheDurationSeconds;
        }

        private string GetCachedThermostatData()
        {
            return Settings.Default.ThermostatData;
        }

        private string UpdateLocalThermostatData()
        {
            var thermostatData = GetRawThermostatData();

            Settings.Default.ThermostatData = thermostatData;
            Settings.Default.LastUpdate = DateTime.Now;
            Settings.Default.Save();

            _cacheInvalid = false;
            return thermostatData;
        }

        public string GetThermostatData()
        {
           return CacheIsValid() ? GetCachedThermostatData() : UpdateLocalThermostatData();
        }

        private string GetRawThermostatData()
        {
            return Get().GetAsync(Settings.Default.Path + _thermostatId).Result
                .Content.ReadAsStringAsync().Result;
        }

        public void PutThermostatData(string route, string elementName, object value)
        {
            var jobject = new JObject();
            jobject[elementName] = JToken.FromObject(value);

            PutRawThermostatData(route, jobject);
        }

        private void PutRawThermostatData(string route, JObject value)
        {
            var request = Get().PutAsJsonAsync(Settings.Default.Path + _thermostatId + "/" + route, value).Result;
            
            if(!request.IsSuccessStatusCode)
                throw new Exception(JObject.Parse(request.Content.ReadAsStringAsync().Result).GetValue("error").ToString());
            
            _cacheInvalid = true;
        }
    }
}
