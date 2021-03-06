/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: JDBCMatrixImpl01.java,v 1.2 2003/06/26 11:43:58 rbegg Exp $
//

package org.jboss.jbossts.qa.Hammer01Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBCMatrixImpl01.java,v 1.2 2003/06/26 11:43:58 rbegg Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBCMatrixImpl01.java,v 1.2 2003/06/26 11:43:58 rbegg Exp $
 */


import org.jboss.jbossts.qa.Hammer01.*;
import org.omg.CORBA.IntHolder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class JDBCMatrixImpl01 implements MatrixOperations
{
	public JDBCMatrixImpl01(int width, int height, String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass)
			throws InvocationException
	{
		_width = width;
		_height = height;
		_dbUser = databaseUser;

		try
		{
			if (databaseDynamicClass != null)
			{
				Properties databaseProperties = new Properties();

				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.userName, databaseUser);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.password, databasePassword);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.dynamicClass, databaseDynamicClass);

				_connection = DriverManager.getConnection(databaseURL, databaseProperties);
			}
			else
			{
				_connection = DriverManager.getConnection(databaseURL, databaseUser, databasePassword);
			}
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl01.JDBCMatrixImpl01: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public void finalize()
			throws Throwable
	{
		try
		{
			if (_connection != null)
			{
				;
			}
			_connection.close();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl01.finalize: " + exception);
			throw exception;
		}
	}

	public int get_width()
			throws InvocationException
	{
		return _width;
	}

	public int get_height()
			throws InvocationException
	{
		return _height;
	}

	public void get_value(int x, int y, IntHolder value)
			throws InvocationException
	{
		if ((x < 0) || (x >= _width) || (y < 0) || (y >= _height))
		{
			throw new InvocationException(Reason.ReasonUnknown);
		}

		Statement statement = null;
		ResultSet resultSet = null;

		try
		{
			statement = _connection.createStatement();

			resultSet = statement.executeQuery("SELECT Value FROM " + _dbUser + "_Matrix WHERE X = \'" + x + "\' AND Y = \'" + y + "\'");
			resultSet.next();
			value.value = resultSet.getInt("Value");
			if (resultSet.next())
			{
				throw new Exception();
			}
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl01.get_value: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
		finally
		{
			try
			{
				if (resultSet != null)
				{
					resultSet.close();
				}

				if (statement != null)
				{
					statement.close();
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCMatrixImpl01.get_value: " + exception);
				throw new InvocationException(Reason.ReasonUnknown);
			}
		}
	}

	public void set_value(int x, int y, int value)
			throws InvocationException
	{
		if ((x < 0) || (x >= _width) || (y < 0) || (y >= _height))
		{
			throw new InvocationException(Reason.ReasonUnknown);
		}

		Statement statement = null;

		try
		{
			statement = _connection.createStatement();

			statement.executeUpdate("UPDATE " + _dbUser + "_Matrix SET Value = \'" + value + "\' WHERE X = \'" + x + "\' AND Y = \'" + y + "\'");
		}
		catch (java.sql.SQLException sqlException)
		{
			System.err.println("JDBCMatrixImpl01.set_value: " + sqlException);

			// Check error message to see if it is a "can't serialize access" message
			String message = sqlException.getMessage();

			if ((message != null) && (message.indexOf("can't serialize access") != -1))
			{
				throw new InvocationException(Reason.ReasonConcurrencyControl);
			}
			throw new InvocationException(Reason.ReasonUnknown);
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl01.set_value: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
		finally
		{
			try
			{
				if (statement != null)
				{
					statement.close();
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCMatrixImpl01.get_value: " + exception);
				throw new InvocationException(Reason.ReasonUnknown);
			}
		}
	}

	private int _width;
	private int _height;

	private Connection _connection;
	private String _dbUser;
}
