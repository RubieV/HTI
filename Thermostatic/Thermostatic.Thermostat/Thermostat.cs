using System.Globalization;
using Newtonsoft.Json;
using Thermostatic.Thermostat.Properties;

namespace Thermostatic.Thermostat
{
    public class Thermostat
    {
        private readonly int _id = Settings.Default.ThermostatID;
        private readonly ApiInterface _apiInterface;

        public Thermostat(int id)
        {
            _apiInterface = new ApiInterface(this);
            _id = id;
        }

        public int Id
        {
            get { return _id; }
        }

        public string Day
        {
            get { return _apiInterface.GetThermostatData("current_day"); }
            set { _apiInterface.UpdateThermostat("day", "current_day", value); }
        }

        public string Time
        {
            get { return _apiInterface.GetThermostatData("time"); }
            set { _apiInterface.UpdateThermostat("time", "time", value); }
        }

        public float CurrentTemperature
        {
            get { return float.Parse(_apiInterface.GetThermostatData("current_temperature"), CultureInfo.InvariantCulture); }
        }

        public float TargetTemperature
        {
            get { return float.Parse(_apiInterface.GetThermostatData("target_temperature"), CultureInfo.InvariantCulture); }
            set { _apiInterface.UpdateThermostat("currentTemperature", "current_temperature", value); }
        }

        public float DayTemperature
        {
            get
            {
                return float.Parse(_apiInterface.GetThermostatData("day_temperature"), CultureInfo.InvariantCulture);
            }
            set
            {
                _apiInterface.UpdateThermostat("dayTemperature", "day_temperature", value);
            }
        }

        public float NightTemperature
        {
            get
            {
                return float.Parse(_apiInterface.GetThermostatData("night_temperature"),
                    CultureInfo.InvariantCulture);
            }
            set
            {
                _apiInterface.UpdateThermostat("nightTemperature", "night_temperature", value);
            }
        }

        public bool WeekprogrammIsEnabled
        {
            get
            {
                return _apiInterface.GetThermostatData("week_program_state") != "on";
            }
            set
            {
                _apiInterface.UpdateThermostat("weekProgramState", "week_program_state", value);
            }
        }

        public WeekProgram WeekProgram
        {
            get
            {
                return JsonConvert.DeserializeObject<WeekProgram>(_apiInterface.GetRawThermostatData("week_program"));
            }
            set
            {
                _apiInterface.UpdateThermostat("weekProgram", "week_program", value);
            }
        }
    }
}
