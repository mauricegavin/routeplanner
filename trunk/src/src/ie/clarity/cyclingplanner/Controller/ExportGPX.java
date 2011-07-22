package ie.clarity.cyclingplanner.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;

import ie.clarity.cyclingplanner.DefaultActivity.Installation;
import ie.clarity.cyclingplanner.Model.Trip;

import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Log of a trip.
 *
 * @author Maurice Gavin
 */
public class ExportGPX {
  private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  private final NumberFormat elevationFormatter;
  private final NumberFormat coordinateFormatter;
  private final SimpleDateFormat timestampFormatter;
  private PrintWriter pw = null;
  private Trip trip;
  private Context context;
  
  public ExportGPX(Context context) {
    // GPX readers expect to see fractional numbers with US-style punctuation.
    // That is, they want periods for decimal points, rather than commas.

	this.context = context;
    
	elevationFormatter = NumberFormat.getInstance(Locale.UK);
    elevationFormatter.setMaximumFractionDigits(1);
    elevationFormatter.setGroupingUsed(false);

    coordinateFormatter = NumberFormat.getInstance(Locale.UK);
    coordinateFormatter.setMaximumFractionDigits(5);
    coordinateFormatter.setMaximumIntegerDigits(3);
    coordinateFormatter.setGroupingUsed(false);

    timestampFormatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
    timestampFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
  
  public void write()
  {
	  writeHeader();	  
	  writeBeginTrack();
	  writeOpenSegment();
	  
	  // Write the GPS coordinates
	  ArrayList<Location> path = trip.getGeoData().getPathTaken();
	  int size = path.size();
	  for(int i = 0; i < size; i++)
	  {
		  writeLocation(path.get(i));
	  }
	  
	  writeCloseSegment();
	  writeEndTrack();
	  writeFooter();
  }

  private String formatLocation(Location l) {
    return "lat=\"" + coordinateFormatter.format(l.getLatitude())
      + "\" lon=\"" + coordinateFormatter.format(l.getLongitude()) + "\"";
  }

  public void prepare(Trip trip, FileWriter out) {
    this.trip = trip;
    this.pw = new PrintWriter(out);
  }

  public String getExtension() {
    return ".GPX";
  }

  public void writeHeader() {
    if (pw != null) {
      pw.format("<?xml version=\"1.0\" encoding=\"%s\" standalone=\"yes\"?>\n",
          Charset.defaultCharset().name());
      pw.println("<?xml-stylesheet type=\"text/xsl\" href=\"details.xsl\"?>");
      pw.println("<gpx");
      pw.println(" version=\"1.0\"");
      pw.format(" creator=\"Cycling Route Planner running on %s\"\n", Build.MODEL);
      pw.println(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
      pw.println(" xmlns=\"http://www.topografix.com/GPX/1/1\"");
      pw.print(" xmlns:topografix=\"http://www.topografix.com/GPX/Private/"
          + "TopoGrafix/0/1\"");
      pw.print(" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 ");
      pw.print("http://www.topografix.com/GPX/1/1/gpx.xsd ");
      pw.print("http://www.topografix.com/GPX/Private/TopoGrafix/0/1 ");
      pw.println("http://www.topografix.com/GPX/Private/TopoGrafix/0/1/"
          + "topografix.xsd\">");
      
      // Author etc.
      pw.println("<author>" + stringAsCData("Maurice Gavin") + "</author>");
      pw.println("<email>" + "gavinm1@tcd.ie" + "</email>");
      pw.println("<time>" + timestampFormatter.format(trip.getDateAtStart()) + "</time>");
    }
  }

  public void writeFooter() {
    if (pw != null) {
      pw.println("</gpx>");
    }
  }

  public void writeBeginTrack() {
    if (pw != null) {
      pw.println("<trk>");
      pw.println("<name>" + stringAsCData(trip.getTripID())
          + "</name>");
      pw.println("<desc>" + stringAsCData("The Reason for the Trip is: " + trip.getPersonalisedRoute().getReason()
    		  +	"\nThe Desired Route Type is: " + trip.getPersonalisedRoute().getType())
    		  + "</desc>");
    
		
      pw.println("<src>" + Installation.id(context) + "</src>");
      pw.println("<extensions><topografix:color>c0c0c0</topografix:color></extensions>");
    }
  }

  public void writeEndTrack() {
    if (pw != null) {
      pw.println("</trk>");
    }
  }

  public void writeOpenSegment() {
    pw.println("<trkseg>");
  }

  public void writeCloseSegment() {
    pw.println("</trkseg>");
  }

  public void writeLocation(Location l) {
    if (pw != null) {
      pw.println("<trkpt " + formatLocation(l) + ">");
      Date d = new Date(l.getTime());
      pw.println("<ele>" + elevationFormatter.format(l.getAltitude()) + "</ele>");
      pw.println("<time>" + timestampFormatter.format(d) + "</time>");
      pw.println("</trkpt>");
    }
  }

  public void close() {
    if (pw != null) {
      pw.close();
      pw = null;
    }
  }
/*
  public void writeWaypoint(Waypoint waypoint) {
    if (pw != null) {
      Location l = waypoint.getLocation();
      if (l != null) {
        pw.println("<wpt " + formatLocation(l) + ">");
        pw.println("<ele>" + elevationFormatter.format(l.getAltitude()) + "</ele>");
        pw.println("<time>" + timestampFormatter.format(l.getTime()) + "</time>");
        pw.println("<name>" + StringUtils.stringAsCData(waypoint.getName())
            + "</name>");
        pw.println("<desc>"
            + StringUtils.stringAsCData(waypoint.getDescription()) + "</desc>");
        pw.println("</wpt>");
      }
    }
  }
  */
  
  /**
   * Formats the given text as a CDATA element to be used in a XML file. This
   * includes adding the starting and ending CDATA tags. Please notice that this
   * may result in multiple consecutive CDATA tags.
   *
   * @param unescaped the unescaped text to be formatted
   * @return the formatted text, inside one or more CDATA tags
   */
  public static String stringAsCData(String unescaped) {
    // "]]>" needs to be broken into multiple CDATA segments, like:
    // "Foo]]>Bar" becomes "<![CDATA[Foo]]]]><![CDATA[>Bar]]>"
    // (the end of the first CDATA has the "]]", the other has ">")
    String escaped = unescaped.replaceAll("]]>", "]]]]><![CDATA[>");
    return "<![CDATA[" + escaped + "]]>";
  }

}

