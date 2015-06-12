using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Util;
using Android.Views;
using Android.Widget;

namespace nl.maartenvisscher.thermodroid
{
    public class ControlFragment : Fragment
    {
        public override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);

            // Create your fragment here

        }
        public override View OnCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState)
        {
            return inflator.Inflate(Resource.Layout.control_fragment, container, false);
        }
    }
}