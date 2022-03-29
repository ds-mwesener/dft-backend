// Copyright 2022 Catena-X
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import * as React from 'react';
import { styled } from '@mui/material/styles';
import { AccessTime, CheckBox, HighlightOff, Pending } from '@mui/icons-material';
import Paper from '@mui/material/Paper';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import { ProcessReport, CsvTypes, Status } from '../models/ProcessReport';
import { formatDate } from '../utils/utils';

interface Column {
  id:
    | 'processId'
    | 'csvType'
    | 'numberOfItems'
    | 'numberOfFailedItems'
    | 'numberOfSucceededItems'
    | 'status'
    | 'startDate'
    | 'endDate';
  label: string;
  minWidth?: number;
  align?: 'right' | 'left' | 'center';
  format?: (value: string) => string;
}

const columns: readonly Column[] = [
  { id: 'processId', label: 'Process Id', minWidth: 170 },
  { id: 'csvType', label: 'CSV Type', minWidth: 100 },
  {
    id: 'numberOfItems',
    label: 'Number of Items',
    minWidth: 170,
    align: 'center',
  },
  {
    id: 'numberOfFailedItems',
    label: 'Number of Failed Items',
    minWidth: 170,
    align: 'center',
  },
  {
    id: 'numberOfSucceededItems',
    label: 'Number of Succeeded Items',
    minWidth: 170,
    align: 'center',
  },
  {
    id: 'status',
    label: 'Status',
    minWidth: 100,
    align: 'center',
  },
  {
    id: 'startDate',
    label: 'Start Date',
    minWidth: 170,
    align: 'center',
    format: (value: string) => formatDate(value),
  },
  {
    id: 'endDate',
    label: 'End Date',
    minWidth: 170,
    align: 'center',
    format: (value: string) => formatDate(value),
  },
];

const rowsData: ProcessReport[] = [];

export default function StickyHeadTable({
  rows = rowsData,
  page = 0,
  rowsPerPage = 10,
  totalElements = 0,
  setPage = (p: number) => {},
  setRowsPerPage = (r: number) => {},
}) {
  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(+event.target.value);
    setPage(0);
  };

  const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: {
      backgroundColor: theme.palette.common.black,
      color: theme.palette.common.white,
    },
    [`&.${tableCellClasses.body}`]: {
      fontSize: 14,
    },
  }));

  const StyledTableRow = styled(TableRow)(({ theme }) => ({
    '&:nth-of-type(odd)': {
      backgroundColor: theme.palette.action.hover,
    },
  }));

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      <TableContainer sx={{ maxHeight: 640 }}>
        <Table stickyHeader aria-label="sticky table">
          <TableHead>
            <TableRow>
              {columns.map(column => (
                <StyledTableCell key={column.id} align={column.align} style={{ minWidth: column.minWidth }}>
                  {column.label}
                </StyledTableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map(row => {
              return (
                <StyledTableRow hover role="checkbox" tabIndex={-1} key={row.processId}>
                  {columns.map(column => {
                    const value = row[column.id];
                    return (
                      <StyledTableCell key={column.id} align={column.align}>
                        {(column.id === 'startDate' || column.id === 'endDate') && (
                          <span>
                            <AccessTime fontSize="small" sx={{ color: '#000000' }} /> &nbsp;
                          </span>
                        )}
                        {column.id === 'endDate' && !value && '-'}
                        {column.id === 'csvType' && value === CsvTypes.aspect && <b> ASPECT </b>}
                        {column.id === 'csvType' && value === CsvTypes.childAspect && <b> CHILD ASPECT </b>}
                        {column.id === 'csvType' && value === CsvTypes.unknown && <b> UNKNOWN </b>}
                        {column.id !== 'status' &&
                          column.id !== 'csvType' &&
                          column.format &&
                          typeof value === 'string' &&
                          column.format(value)}
                        {column.id !== 'status' &&
                          column.id !== 'csvType' &&
                          (!column.format || typeof value !== 'string') &&
                          value}
                        {column.id === 'status' && value === Status.completed && (
                          <span title={Status.completed}>
                            <CheckBox fontSize="small" sx={{ color: '#8bc34a' }} />
                          </span>
                        )}
                        {column.id === 'status' && value === Status.failed && (
                          <span title={Status.failed}>
                            <HighlightOff fontSize="small" sx={{ color: '#f44336' }} />
                          </span>
                        )}
                        {column.id === 'status' && value === Status.inProgress && (
                          <span title={Status.inProgress}>
                            <Pending fontSize="small" sx={{ color: '#2196f3' }} />
                          </span>
                        )}
                      </StyledTableCell>
                    );
                  })}
                </StyledTableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[10, 15, 20]}
        component="div"
        count={totalElements}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Paper>
  );
}
