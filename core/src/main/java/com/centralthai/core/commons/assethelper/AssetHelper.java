package com.centralthai.core.commons.assethelper;

import com.centralthai.core.models.ImageModel;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Utility class to help with asset related operations
 */
public class AssetHelper {
    private static final Escaper URL_ESCAPER = UrlEscapers.urlFragmentEscaper();

    /**
     * Empty constructor initialization
     */
    private AssetHelper() {

    }

    /**
     * @param resource asset resource to get asset details
     * @return ImageModel consisting of the image path and image title for alt text
     */
    public static ImageModel getImagePropertiesFromResource(Resource resource) {
        ImageModel imageModel = new ImageModel();
        setImagePropertiesFromResource(resource, imageModel);
        return imageModel;
    }

    /**
     * @param resource   asset resource
     * @param imageModel ImageModel consisting of the image path and image title for alt text
     */
    public static void setImagePropertiesFromResource(Resource resource, ImageModel imageModel) {
        Asset asset = adaptResourceToAsset(resource);
        if (Optional.ofNullable(asset).isPresent())
            setImageProperties(asset, imageModel);
    }

    /**
     * Method is used to read metadata of an asset and set in the imagemodel
     *
     * @param asset
     * @param imageModel
     */
    public static void setImageProperties(Asset asset, ImageModel imageModel) {
        if (Optional.ofNullable(asset).isPresent()) {
            String title = asset.getMetadataValue(DamConstants.DC_TITLE);
            String description = asset.getMetadataValue(DamConstants.DC_DESCRIPTION);
            if (StringUtils.isEmpty(title))
                title = FilenameUtils.removeExtension(asset.getName());

            if (StringUtils.isEmpty(description))
                description = StringUtils.EMPTY;
            imageModel.setAlt(title);
            imageModel.setImagePath(URL_ESCAPER.escape(asset.getPath()));
            imageModel.setDescription(description);
            String imageExtension = FilenameUtils.getExtension(asset.getName());
            if (imageExtension.equalsIgnoreCase(AssestHelperConstants.GIF_EXTENSTION)) {
                imageModel.setExtension(AssestHelperConstants.PNG_EXTENSTION);
            } else {
                imageModel.setExtension(imageExtension);
            }
        }

    }

    /**
     * @param asset
     * @param renditionName
     * @return redition path
     */
    public static String getAssetRendition(Asset asset, String renditionName) {
        Rendition rendition = asset.getRendition(renditionName);
        if (rendition != null) {
            return rendition.getPath();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Method to get the name of the folder
     *
     * @param folderResource
     * @return folder name
     */
    public static String getFolderTitle(Resource folderResource) {
        String folderTitle = folderResource.getName();
        Resource folderContentResource = folderResource.getChild(JcrConstants.JCR_CONTENT);
        if (Optional.ofNullable(folderContentResource).isPresent()) {
            folderTitle =
                    folderContentResource.getValueMap()
                            .get(JcrConstants.JCR_TITLE, folderTitle);
        }
        return folderTitle;
    }

    /**
     * Method resolves resource to an asset and resturns it
     *
     * @param resource of an asset
     * @return asset
     */
    public static Asset adaptResourceToAsset(Resource resource) {
        Asset asset = null;
        if (Optional.ofNullable(resource).isPresent()) {
            asset = resource.adaptTo(Asset.class);
        }
        return asset;
    }

    /**
     * @param asset is used to get the asset mime type
     * @return mimeType
     */
    public static String getAssetMimeType(final Asset asset) {
        String mimeType = StringUtils.EMPTY;
        final Optional<Asset> assetPath = Optional.ofNullable(asset);
        if (assetPath.isPresent()) {
            mimeType = assetPath.get().getMimeType();
        }
        return mimeType;
    }

    /**
     * Method list down the assets under the specified root folder
     *
     * @param resource Folder root resource
     * @return list of assets under the root folder
     */
    public static List<ImageModel> getAssetsFromRootFolder(Resource resource) {
        final Iterator<Resource> resourceIterator = resource.listChildren();
        final Iterable<Resource> iterable = () -> resourceIterator;
        return StreamSupport.stream(iterable.spliterator(), false)
                .filter(Objects::nonNull)
                .filter(childNode -> !childNode.getName().equalsIgnoreCase("jcr:content"))
                .map(assetResource -> getImagePropertiesFromResource(assetResource))
                .collect(Collectors.toList());
    }
}
