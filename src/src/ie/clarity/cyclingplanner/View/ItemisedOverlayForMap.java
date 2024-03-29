package ie.clarity.cyclingplanner.View;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class ItemisedOverlayForMap extends ItemizedOverlay {

	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private Context context = null;
	
	public ItemisedOverlayForMap(Drawable defaultMarker, Context _context) {
		super(boundCenter(defaultMarker));
		this.context = _context;
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		 return overlays.get(i);
	}

	@Override
	public int size() {
		return overlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
	    overlays.add(overlay);
	    populate();
	}
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = overlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(context);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}

	@Override
    public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
        // Don't draw the shadow layer
        if (!shadow) {
            super.draw(canvas, mapView, shadow);
        }
    }
	
	public void clear()
	{
		overlays.clear();
	}
}

