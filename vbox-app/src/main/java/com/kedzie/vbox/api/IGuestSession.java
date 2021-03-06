package com.kedzie.vbox.api;

import com.kedzie.vbox.api.jaxb.DirectoryCreateFlag;
import com.kedzie.vbox.api.jaxb.DirectoryOpenFlag;
import com.kedzie.vbox.api.jaxb.DirectoryRemoveRecFlag;
import com.kedzie.vbox.api.jaxb.FsObjRenameFlag;
import com.kedzie.vbox.soap.KSOAP;

import java.util.List;

/**
 * A guest session represents one impersonated user account on the guest, so every operation will use the same credentials specified when 
 * creating the session object via {@link IGuest#createSession}. 
 */
@KSOAP
public interface IGuestSession extends IManagedObjectRef {

    /**
     * Returns the user name used by this session to impersonate users on the guest.
     */
    @KSOAP(cacheable=true) public String getUser();
    
    /**
     * Returns the domain name used by this session to impersonate users on the guest.
     */
    @KSOAP(cacheable=true) public String getDomain();
    
    /**
     * Returns the session's friendly name.
     */
    @KSOAP(cacheable=true) public String getName();
    
    /**
     * Returns the internal session ID.
     */
    @KSOAP(cacheable=true) public Integer getId();
    
    /**
     * Returns the session timeout (in ms).
     * <dl class="user"><dt><b>Expected result codes:</b></dt><dd><table class="doxtable">
     * <tbody><tr>
     * <td>{@link IVirtualBox#E_NOTIMPL} </td><td>The method is not implemented yet.  </td></tr>
     * </tbody></table>
     * </dd></dl>
     */
    @KSOAP(cacheable=true) public int getTimeout();
    public void setTimeout(@KSOAP(type="unsignedint", value="timeout") int timeout);
    
    /**
     * Returns the current session environment.
     */
    @KSOAP(cacheable=true) public List<String> getEnvironment();
    public void setEnvironment(@KSOAP("environment") String...environment);
    
    /**
     * Returns all current guest processes.
     */
    @KSOAP(cacheable=true) public List<IGuestProcess> getProcesses();
    
    /**
     * Returns all currently opened guest directories.
     */
    @KSOAP(cacheable=true) public List<IGuestDirectory> getDirectories();
    
    /**
     * Returns all currently opened guest files.
     */
    @KSOAP(cacheable=true) public List<IGuestFile> getFiles();
    
    /**
     * Closes this session.
     * All opened guest directories, files and processes which are not referenced by clients anymore will be uninitialized.
     */
    public void close();
    
    /**
     * Copies a file from guest to the host.
     * @param source	Source file on the guest to copy to the host.
     *
     * @param destination	Destination file name on the host.
     * @param flags		Copy flags; see <a class="el" href="_virtual_box_8idl.html#af4001a07f3e4bc28ecc98faf1d6c7635">CopyFileFlag</a><b></b> for more information.
     * @return		Progress object to track the operation completion.
    * <dl><dt><b>Expected result codes:</b></dt><dd><table><tbody><tr>
    * <td>{@link IVirtualBox#VBOX_E_IPRT_ERROR }</td><td>Error starting the copy operation.</td></tr>
    * </tbody></table></dd></dl>
     */
    public IProgress copyFromGuest(@KSOAP("sources") List<String> sources, @KSOAP("filters") List<String> filters, @KSOAP("flags") List<String> flags, @KSOAP("destination") String destination);
    
    /**
     * Copies a file from host to the guest.
     * @param sources	Source file on the host to copy to the guest.
     * @param filters	filters
     * @param flags	Copy flags;
     * @return	Progress object to track the operation completion.
     * <dl><dt><b>Expected result codes:</b></dt><dd><table><tbody><tr>
     * <td>{@link IVirtualBox#VBOX_E_IPRT_ERROR}</td><td>Error starting the copy operation</td></tr>
     * </tbody></table></dd></dl>
     */
    public IProgress copyToGuest(@KSOAP("sources") List<String> sources, @KSOAP("filters") List<String> filters, @KSOAP("flags") List<String> flags, @KSOAP("destination") String destination);
    
    /**
     * Create a directory on the guest.
     * @param path		Full path of directory to create.
     * @param mode		File creation mode.
     * @param flags		Creation flags; see {@link DirectoryCreateFlag} for more information.
    * <dl><dt><b>Expected result codes:</b></dt><dd><table><tbody><tr>
    * <td>{@link IVirtualBox#VBOX_E_IPRT_ERROR}</td><td>Error while creating the directory.</td></tr>
    * </tbody></table></dd></dl>
     */
    public void directoryCreate(@KSOAP("path") String path, @KSOAP(type="unsignedint", value="mode") int mode, @KSOAP("flags") DirectoryCreateFlag...flags);
    
    /**
     * Create a temporary directory on the guest.
     * @param templateName		Template for the name of the directory to create. This must contain at least one 'X' character. The first group of consecutive 'X' characters in the template will be replaced by a random alphanumeric string to produce a unique name.
     * @param mode		The mode of the directory to create. Use 0700 unless there are reasons not to. This parameter is ignored if "secure" is specified.
     * @param path		The absolute path to create the temporary directory in.
     * @param secure		Whether to fail if the directory can not be securely created. Currently this means that another unprivileged user cannot manipulate the path specified or remove the temporary directory after it has been created. Also causes the mode specified to be ignored. May not be supported on all guest types.
     * @return		On success this will contain the name of the directory created with full path.
    * <dl><dt><b>Expected result codes:</b></dt><dd><table><tbody><tr>
    * <td>{@link IVirtualBox#VBOX_E_IPRT_ERROR}</td><td>The temporary directory could not be created. Possible reasons include a non-existing path or an insecure path when the secure option was requested</td></tr>
    * <td>{@link IVirtualBox#VBOX_E_NOT_SUPPORTED}</td><td>The operation is not possible as requested on this particular guest type.</td></tr>
    * <td>{@link IVirtualBox#E_INVALIDARG}</td><td>Invalid argument. This includes an incorrectly formatted template, or a non-absolute path. </td></tr>
    * </tbody></table></dd></dl>
     */
    public String directoryCreateTemp(@KSOAP("templateName") String templateName, @KSOAP(type="unsignedint", value="mode") int mode, 
            @KSOAP("path") String path, @KSOAP(type="boolean", value="secure") boolean secure);

    /**
     * Checks whether a directory exists on the guest or not.
     * @param path		Directory to check existence for.
     * @return				Returns <code>true</code> if the directory exists, <code>false</code> if not.
    * <dl><dt><b>Expected result codes:</b></dt><dd><table><tbody><tr>
    * <td>{@link IVirtualBox#VBOX_E_IPRT_ERROR}</td><td>Error while checking existence of the directory specified. </td></tr>
    * </tbody></table></dd></dl>
     */
    public boolean directoryExists(@KSOAP("path") String path);
    
    /**
     * Opens a directory and creates a {@link IGuestDirectory} object that can be used for further operations.
     * @param path		Full path to file to open.
     * @param filter		Open filter to apply. This can include wildcards like ? and *.
     * @param flags		Open flags; see {@link DirectoryOpenFlag} for more information.
     * @return			{@link IGuestDirectory} object containing the opened directory.
     */
    public IGuestDirectory directoryOpen(@KSOAP("path") String path, @KSOAP("filter") String filter, @KSOAP("flags") DirectoryOpenFlag...flags);
    
    /**
     * Queries information of a directory on the guest.
     * <dl><dt><b>Expected result codes:</b></dt><dd><table><tbody><tr>
    * <td>{@link IVirtualBox#VBOX_E_OBJECT_NOT_FOUND}</td><td>Directory to query information for was not found.</td></tr>
    * <td>{@link IVirtualBox#VBOX_E_IPRT_ERROR}</td><td>Error querying information.</td></tr>
    * </tbody></table></dd></dl>
     * @param path		Directory to query information for.
     * @return		information
     */
    public IGuestFsObjInfo directoryQueryInfo(@KSOAP("path") String path);
    
    public void directoryRemove(@KSOAP("path") String path);
    
    public IProgress directoryRemoveRecursive(@KSOAP("path") String path, @KSOAP("flags") DirectoryRemoveRecFlag...flags);
    

    public void directorySetACL(@KSOAP("path") String path, @KSOAP("acl") String acl);
    
    public void environmentClear();
    
    public String environmentGet(@KSOAP("name") String name);
    
    public void environmentSet(@KSOAP("name") String name, @KSOAP("value") String value);
    
    public void environmentUnSet(@KSOAP("name") String name);
    
    public IGuestFile fileCreateTemp(@KSOAP("templateName") String templateName, @KSOAP(type="unsignedint", value="mode") int mode, @KSOAP("path") String path, @KSOAP(type="boolean", value="secure") boolean secure);
    
    public boolean fileExists(@KSOAP("path") String path);
    
    public void fileRemove(@KSOAP("path") String path);
    
    public IGuestFile fileOpen(@KSOAP("path") String path, @KSOAP("openMode") String openMode, @KSOAP("disposition") String disposition,
            @KSOAP(type="unsignedint", value="creationMode") int creationMode, @KSOAP(type="long", value="offset") long offset);
    
    public IFsObjInfo fileQueryInfo(@KSOAP("path") String path);
    
    public void fsObjRename(@KSOAP("oldPath") String oldPath, @KSOAP("newPath") String newPath, @KSOAP("flags") FsObjRenameFlag...flags);
    
    public long fileQuerySize(@KSOAP("path") String path);
    
    public void fileSetACL(@KSOAP("path") String path, @KSOAP("acl") String acl);

    /**
     * Removes a symbolic link on the guest if it's a file.
     * <dl><dt><b>Expected result codes:</b></dt><dd><table><tbody><tr>
     * <td>{@link IVirtualBox#E_NOTIMPL}</td><td>The method is not implemented yet.  </td></tr>
     * </tbody></table></dd></dl>
     * @param file		Symbolic link to remove.
     */
    public void symlinkRemoveFile(@KSOAP("file") String file);
}
